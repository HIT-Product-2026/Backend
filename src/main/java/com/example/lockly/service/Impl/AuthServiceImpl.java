package com.example.lockly.service.Impl;

import com.example.lockly.common.util.PasswordUtil;
import com.example.lockly.constant.CommonConstant;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.InvalidatedToken;
import com.example.lockly.domain.entity.OtpPurpose;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.exception.VsException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.JwtProvider;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.EmailService;
import com.example.lockly.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
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
    OtpService otpService;
    EmailService emailService;
    PasswordUtil passwordUtil;
    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    // register:{otpCode} → RegisterPendingData {email, passwordHash}  TTL 5p
    static final String REGISTER_PREFIX = "register:";
    static final Duration REGISTER_TTL  = Duration.ofMinutes(5);

    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @NonFinal @Value("${jwt.access.expiration_time}")  long ACCESS_TOKEN_EXPIRATION;
    @NonFinal @Value("${jwt.refresh.expiration_time}") long REFRESH_TOKEN_EXPIRATION;

    // =========================================================
    // ĐĂNG KÝ — Bước 1
    // Nhận: email + password + confirmPassword
    // Xử lý: validate → sinh OTP → lưu {email, passwordHash} vào Redis với key=OTP → gửi OTP về Gmail
    // =========================================================
    @Override
    public void sendOtpForRegister(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email đã tồn tại / Email đã được đăng ký.");
        }

        String otpCode    = String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);
        String redisKey   = REGISTER_PREFIX + otpCode;
        RegisterPendingData pendingData = new RegisterPendingData(
                request.email(),
                passwordUtil.hash(request.password())
        );

        redisTemplate.opsForValue().set(redisKey, pendingData, REGISTER_TTL);
        log.info("[Register Bước 1] Lưu Redis key={}, email={}", redisKey, request.email());

        emailService.sendOtpEmail(request.email(), otpCode, "đăng ký");
    }

    @Override
    @Transactional
    public UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request) {

        String redisKey  = REGISTER_PREFIX + request.otp();
        Object raw = redisTemplate.opsForValue().get(redisKey);
        if (raw == null) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        RegisterPendingData pendingData = objectMapper.convertValue(raw, RegisterPendingData.class);

        String baseUsername = pendingData.email().split("@")[0];
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix++;
        }

        User user = User.builder()
                .username(username)
                .displayName(username)
                .email(pendingData.email())
                .passwordHash(pendingData.passwordHash())
                .build();
        userRepository.save(user);
        log.info("[Register Bước 2] Đã tạo user mới, email={}", pendingData.email());

        redisTemplate.delete(redisKey);
        log.info("[Register Bước 2] Đã xóa Redis key={}", redisKey);

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
        otpService.sendOtp(request.email(), OtpPurpose.FORGOT_PASSWORD);
    }

    @Override
    @Transactional
    public void verifyOtpForgotPassword(VerifyOtpForgotPasswordRequestDto request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.FORGOT_PASSWORD);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException(ErrorMessage.Auth.ERR_PASSWORD_NOT_MATCH);
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));
        user.setPasswordHash(passwordUtil.hash(request.newPassword()));
        userRepository.save(user);
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
}