package com.example.lockly.service.Impl;

import com.example.lockly.common.util.PasswordUtil;
import com.example.lockly.constant.CommonConstant;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.request.auth.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.TokenType;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.exception.nonRetryException.UnauthorizedException;
import com.example.lockly.exception.nonRetryException.VsException;
import com.example.lockly.mapper.UserResponseMapper;
import com.example.lockly.repository.main.InvalidatedTokenRepository;
import com.example.lockly.repository.main.ProfileRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.security.JwtProvider;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.EmailService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final PasswordUtil passwordUtil;
    private final RedisService redisService;
    private final ProfileRepository profileRepository;
    private final UserDetailsService userDetailsService;
    private final MinIOService minIOService;

    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${jwt.access.expiration_time}")  long ACCESS_TOKEN_EXPIRATION;
    @Value("${jwt.refresh.expiration_time}") long REFRESH_TOKEN_EXPIRATION;

    @Override
    public void sendOtpForRegister(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email đã tồn tại.");
        }

        String otpCode = String.valueOf(
                SECURE_RANDOM.nextInt(900000) + 100000
        );

        redisService.saveRegister(
                request.email(),
                RegisterCacheDto.builder()
                        .otp(otpCode)
                        .passwordHash(passwordUtil.hash(request.password()))
                        .build()
        );

        emailService.sendOtpEmail(
                request.email(),
                otpCode,
                "đăng ký"
        );
    }

    @Override
    @Transactional
    public UserResponseDto verifyOtpAndRegister(VerifyOtpRequestDto request) {

        RegisterCacheDto registerCache = redisService.getRegister(request.email());

        if (registerCache == null) {
            throw new BadRequestException("Phiên đăng ký đã hết hạn. Vui lòng thử lại.");
        }

        if (!registerCache.otp().equals(request.otp())) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }

        String displayName = request.email().split("@")[0];

        // Username là chuỗi ngẫu nhiên 10 chữ số
        Random random = new Random();
        String username;

        do {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < 10; i++) {
                builder.append(random.nextInt(10));
            }

            username = builder.toString();

        } while (userRepository.existsByUsername(username));

        User user = User.builder()
                .username(username)
                .displayName(displayName)
                .email(request.email())
                .passwordHash(registerCache.passwordHash())
                .mode(UserMode.PUBLIC)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .birthday(null)
                .gender(null)
                .phoneNumber(null)
                .postCount(0)
                .build();

        userRepository.save(user);
        profileRepository.save(profile);

        redisService.deleteRegister(request.email());

        redisService.saveUser(UserCacheDto.from(user));

        log.info("[Register] Đăng ký thành công, email={}", request.email());

        return UserResponseMapper.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException(ErrorMessage.Auth.ERR_INVALID_CREDENTIALS));

        if (!passwordUtil.verify(request.password(), user.getPasswordHash())) {
            throw new BadRequestException(ErrorMessage.Auth.ERR_INVALID_CREDENTIALS);
        }

        redisService.saveUser(UserCacheDto.from(user));

        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequestDto request) {

        String token = request.token();

        log.info("LOGOUT REQUEST RECEIVED");

        try {
            String jwtId = jwtProvider.extractTokenId(token);
            String username = jwtProvider.extractUsername(token);

            Date expirationDate = jwtProvider.extractExpiration(token);
            boolean expired = jwtProvider.isTokenExpired(token);

            log.info("jwtId = {}", jwtId);
            log.info("username = {}", username);
            log.info("expired = {}", expired);
            log.info("expirationDate = {}", expirationDate);

            if (expired) {
                log.warn("Logout failed: token already expired");
                throw new VsException(
                        HttpStatus.UNAUTHORIZED,
                        ErrorMessage.Auth.ERR_TOKEN_INVALIDATED
                );
            }

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            UUID userId = ((CustomUserDetails) userDetails).getId();

            String redisAccessJti = redisService.getAccessToken(userId);

            if (!jwtId.equals(redisAccessJti)) {
                throw new UnauthorizedException("Token đã hết hiệu lực.");
            }

            // Xóa whitelist trong Redis
            redisService.deleteAccessToken(userId);
            redisService.deleteRefreshToken(userId);

            log.info("Logout success, userId={}, jwtId={}", userId, jwtId);

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

        String otpCode = String.valueOf(
                SECURE_RANDOM.nextInt(900000) + 100000
        );

        redisService.saveForgotPassword(
                request.email(),
                ForgotPasswordCacheDto.builder()
                        .otp(otpCode)
                        .build()
        );

        log.info("[ForgotPassword Bước 1] Lưu Redis email={}", request.email());

        emailService.sendOtpEmail(
                request.email(),
                otpCode,
                "đặt lại mật khẩu"
        );
    }

    @Override
    @Transactional
    public void verifyOtpForgotPassword(VerifyOtpRequestDto request) {

        ForgotPasswordCacheDto cache = redisService.getForgotPassword(request.email());

        if (cache == null || !cache.otp().equals(request.otp())) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }

        redisService.deleteForgotPassword(request.email());

        redisService.saveForgotPasswordVerified(request.email());

        log.info("[ForgotPassword Bước 2] Xác thực OTP thành công, email={}", request.email());
    }
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {

        Boolean verified = redisService.isForgotPasswordVerified(request.email());

        if (!Boolean.TRUE.equals(verified)) {
            throw new BadRequestException("Phiên xác thực đã hết hạn. Vui lòng gửi lại OTP.");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", request.email()));

        user.setPasswordHash(passwordUtil.hash(request.newPassword()));

        userRepository.save(user);

        redisService.deleteForgotPasswordVerified(request.email());

        redisService.saveUser(UserCacheDto.from(user));

        log.info("[ForgotPassword Bước 3] Đặt lại mật khẩu thành công, email={}", request.email());
    }

    private LoginResponseDto buildLoginResponse(User user) {

        String accessToken = jwtProvider.generateToken(
                user,
                ACCESS_TOKEN_EXPIRATION,
                TokenType.ACCESS
        );

        String refreshToken = jwtProvider.generateToken(
                user,
                REFRESH_TOKEN_EXPIRATION,
                TokenType.REFRESH
        );

        String accessJti = jwtProvider.extractTokenId(accessToken);
        String refreshJti = jwtProvider.extractTokenId(refreshToken);

        redisService.saveAccessToken(
                user.getId(),
                accessJti,
                ACCESS_TOKEN_EXPIRATION
        );

        redisService.saveRefreshToken(
                user.getId(),
                refreshJti,
                REFRESH_TOKEN_EXPIRATION
        );

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponseMapper.from(user))
                .avatarUrl(minIOService.generatePresignedUrl(user.getObjectNameAvatar()))
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
            return userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        }

        throw new RuntimeException("Invalid authentication principal");
    }

    @Override
    public UUID getCurrentUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();

        }

        throw new RuntimeException("Invalid authentication principal");
    }

    @Override
    @Transactional
    public LoginResponseDto refreshToken(RefreshTokenRequestDto request) {

        // Lấy Refresh Token từ request
        String refreshToken = request.refeshToken();

        // Lấy username từ JWT
        String username = jwtProvider.extractUsername(refreshToken);

        // Token không chứa username => JWT không hợp lệ
        if (username == null) {
            throw new UnauthorizedException("Refresh token không hợp lệ.");
        }

        // Load thông tin user để phục vụ xác thực JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Kiểm tra chữ ký, thời hạn và blacklist của JWT
        if (!jwtProvider.isTokenValid(refreshToken, userDetails)) {
            throw new UnauthorizedException("Refresh token không hợp lệ.");
        }

        UUID userId = ((CustomUserDetails) userDetails).getId();

        if (!redisService.existsRefreshToken(userId))
            throw new UnauthorizedException("Refresh token không hợp lệ.");

        // Chỉ cho phép sử dụng Refresh Token để gọi API refresh
        TokenType type = jwtProvider.extractTokenType(refreshToken);
        if (type != TokenType.REFRESH) {
            throw new UnauthorizedException("Đây không phải Refresh Token.");
        }

        // Lấy User từ database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UnauthorizedException("Không tìm thấy người dùng."));

        // Lấy jti của Refresh Token gửi lên
        String refreshJti = jwtProvider.extractTokenId(refreshToken);

        // Lấy jti của Refresh Token hiện tại đang được lưu trong Redis
        String redisRefreshJti = redisService.getRefreshToken(user.getId());

        // Refresh Token đã bị thay thế hoặc hết hạn trong Redis
        if (!refreshJti.equals(redisRefreshJti)) {
            throw new UnauthorizedException("Refresh token đã hết hiệu lực.");
        }

        // Sinh Access Token và Refresh Token mới,
        // đồng thời cập nhật lại jti mới vào Redis
        return buildLoginResponse(user);
    }

    @Override
    public User getUserFromCache(){
        // Lấy user từ cache thay vì db
        UUID userId = this.getCurrentUserId();
        User user;
        UserCacheDto userCache = redisService.getUser(userId);
        if (userCache == null){
            user = this.getCurrentUser();
            redisService.saveUser(UserCacheDto.from(user));
        } else {
            user = userCache.toEntity();
        }
        return user;
    }

    @Override
    @Transactional
    public void forceLogout(UUID userId) {

        redisService.deleteAccessToken(userId);
        redisService.deleteRefreshToken(userId);

        log.info("Force logout user {}", userId);
    }

    @Override
    @Transactional
    public void forceResetPassword(User user) {

        String randomPassword = UUID.randomUUID().toString();

        user.setPasswordHash(passwordUtil.hash(randomPassword));

        userRepository.save(user);

        redisService.saveUser(UserCacheDto.from(user));

        redisService.deleteAccessToken(user.getId());
        redisService.deleteRefreshToken(user.getId());

        log.warn("Force reset password, userId={}", user.getId());
    }
}