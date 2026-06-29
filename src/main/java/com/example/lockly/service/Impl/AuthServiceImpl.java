package com.example.lockly.service.Impl;

import com.example.lockly.common.util.PasswordUtil;
import com.example.lockly.constant.CommonConstant;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.domain.dto.request.auth.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.InvalidatedToken;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.exception.VsException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.security.JwtProvider;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.EmailService;
import com.example.lockly.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    JwtProvider jwtProvider;
    InvalidatedTokenRepository invalidatedTokenRepository;
    EmailService emailService;
    PasswordUtil passwordUtil;
    RedisTemplate<String, Object> redisTemplate;
    UserService userService;

    static final String REGISTER_PREFIX        = "register:";
    static final Duration REGISTER_TTL         = Duration.ofMinutes(5);

    static final String FORGOT_PREFIX          = "forgot:";
    static final String FORGOT_VERIFIED_PREFIX = "forgot:verified:";
    static final Duration FORGOT_OTP_TTL       = Duration.ofMinutes(5);
    static final Duration FORGOT_VERIFIED_TTL  = Duration.ofMinutes(10);

    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @NonFinal @Value("${jwt.access.expiration_time}")  long ACCESS_TOKEN_EXPIRATION;
    @NonFinal @Value("${jwt.refresh.expiration_time}") long REFRESH_TOKEN_EXPIRATION;

    @Override
    public void sendOtpForRegister(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email đã tồn tại / Email đã được đăng ký.");
        }

        String otpCode = String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(REGISTER_PREFIX + request.email(), otpCode, REGISTER_TTL);
        redisTemplate.opsForValue().set(REGISTER_PREFIX + "pwd:" + request.email(), passwordUtil.hash(request.password()), REGISTER_TTL);
        log.info("[Register Bước 1] Lưu Redis email={}", request.email());
        emailService.sendOtpEmail(request.email(), otpCode, "đăng ký");
    }

    @Override
    @Transactional
    public UserResponseDto verifyOtpAndRegister(VerifyOtpRequestDto request) {
        String otpKey = REGISTER_PREFIX + request.email();
        String pwdKey = REGISTER_PREFIX + "pwd:" + request.email();

        Object rawOtp = redisTemplate.opsForValue().get(otpKey);
        if (rawOtp == null || !rawOtp.toString().equals(request.otp())) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }

        Object rawPwd = redisTemplate.opsForValue().get(pwdKey);
        if (rawPwd == null) {
            throw new BadRequestException("Phiên đăng ký đã hết hạn. Vui lòng thử lại.");
        }

        String baseUsername = request.email().split("@")[0];
        String username = baseUsername;

        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }

        User user = User.builder()
                .username(username)
                .displayName(username)
                .email(request.email())
                .passwordHash(rawPwd.toString())
                .build();

        userRepository.save(user);
        log.info("[Register Bước 2] Đã tạo user mới, email={}", request.email());

        redisTemplate.delete(otpKey);
        redisTemplate.delete(pwdKey);
        log.info("[Register Bước 2] Đã xóa Redis key email={}", request.email());

        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException(ErrorMessage.Auth.ERR_INVALID_CREDENTIALS));

        if (!passwordUtil.verify(request.password(), user.getPasswordHash())) {
            throw new BadRequestException(ErrorMessage.Auth.ERR_INVALID_CREDENTIALS);
        }

        // Cập nhật fcm token
//        userService.updateFcmTokenByUserId(user.getId(), request.fcmToken());
        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequestDto request) {

        String token = request.token();
        log.info("LOGOUT REQUEST RECEIVED");

        try {
            String jwtId = jwtProvider.extractTokenId(token);
            Date expirationDate = jwtProvider.extractExpiration(token);
            boolean expired = jwtProvider.isTokenExpired(token);
            boolean alreadyBlacklisted = invalidatedTokenRepository.existsById(jwtId);

            log.info("token = {}", token);
            log.info("jwtId = {}", jwtId);
            log.info("expired = {}", expired);
            log.info("expirationDate = {}", expirationDate);
            log.info("alreadyBlacklisted = {}", alreadyBlacklisted);

            if (expired) {
                log.warn("Logout failed: token already expired");
                throw new VsException(HttpStatus.UNAUTHORIZED, ErrorMessage.Auth.ERR_TOKEN_INVALIDATED);
            }
            if (alreadyBlacklisted) {
                log.warn("Logout failed: token already invalidated (blacklisted)");
                throw new VsException(HttpStatus.BAD_REQUEST, ErrorMessage.Auth.ERR_TOKEN_ALREADY_INVALIDATED);
            }

            LocalDateTime expirationTime = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            invalidatedTokenRepository.save(new InvalidatedToken(jwtId, expirationTime));
            log.info("Logout success → token blacklisted, jwtId = {}", jwtId);

        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void sendOtpForForgotPassword(ForgotPasswordRequestDto request) {

        if (!userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email chưa được đăng ký.");
        }

        String otpCode  = String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);
        String redisKey = FORGOT_PREFIX + request.email();

        redisTemplate.opsForValue().set(redisKey, otpCode, FORGOT_OTP_TTL);
        log.info("[ForgotPassword Bước 1] Lưu Redis key={}", redisKey);

        emailService.sendOtpEmail(request.email(), otpCode, "đặt lại mật khẩu");
    }

    @Override
    @Transactional
    public void verifyOtpForgotPassword(VerifyOtpRequestDto request) {

        String redisKey = FORGOT_PREFIX + request.email();
        Object raw = redisTemplate.opsForValue().get(redisKey);

        if (raw == null || !raw.toString().equals(request.otp())) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }

        redisTemplate.delete(redisKey);
        redisTemplate.opsForValue().set(FORGOT_VERIFIED_PREFIX + request.email(), "true", FORGOT_VERIFIED_TTL);
        log.info("[ForgotPassword Bước 2] Xác thực OTP thành công, email={}", request.email());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        String verifiedKey = FORGOT_VERIFIED_PREFIX + request.email();
        Object verified = redisTemplate.opsForValue().get(verifiedKey);

        if (verified == null) {
            throw new BadRequestException("Phiên xác thực đã hết hạn. Vui lòng gửi lại OTP.");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        user.setPasswordHash(passwordUtil.hash(request.newPassword()));

        userRepository.save(user);
        redisTemplate.delete(verifiedKey);
        log.info("[ForgotPassword Bước 3] Đặt lại mật khẩu thành công, email={}", request.email());
    }

    private LoginResponseDto buildLoginResponse(User user) {

        String accessToken  = jwtProvider.generateToken(user, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtProvider.generateToken(user, REFRESH_TOKEN_EXPIRATION);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .tokenType(CommonConstant.BEARER_TOKEN)
                .build();
    }

    @Override
    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }

        throw new RuntimeException("Invalid authentication principal");
    }
}