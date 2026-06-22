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
import com.example.lockly.exception.DuplicateResourceException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.exception.VsException;
import com.example.lockly.repository.InvalidatedTokenRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.JwtProvider;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    PasswordUtil passwordUtil;

    @NonFinal @Value("${jwt.access.expiration_time}")  long ACCESS_TOKEN_EXPIRATION;
    @NonFinal @Value("${jwt.refresh.expiration_time}") long REFRESH_TOKEN_EXPIRATION;

    @Override
    @Transactional
    public void sendOtpForRegister(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email đã tồn tại/ Email đã được đăng ký.");
        }
        otpService.sendOtp(request.email(), OtpPurpose.REGISTER);
    }

    @Override
    @Transactional
    public UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException(ErrorMessage.Auth.ERR_PASSWORD_NOT_MATCH);
        }
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.REGISTER);

        // Tự sinh username từ phần local của email
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
                .passwordHash(passwordUtil.hash(request.password()))
                .build();

        userRepository.save(user);
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
            throw new BadRequestException("Nếu email tồn tại, mã OTP sẽ được gửi tới Gmail của bạn.");
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