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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    JwtProvider jwtProvider;
    InvalidatedTokenRepository invalidatedTokenRepository;
    OtpService otpService;
    PasswordUtil passwordUtil;

    @NonFinal
    @Value("${jwt.access.expiration_time}")
    long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh.expiration_time}")
    long REFRESH_TOKEN_EXPIRATION;

    @Override
    @Transactional
    public void sendOtpForRegister(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        otpService.sendOtp(request.email(), OtpPurpose.REGISTER);
    }

    @Override
    @Transactional
    public UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.REGISTER);

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }

        User user = User.builder()
                .username(request.username())
                .displayName(request.displayName())
                .email(request.email())
                .password(passwordUtil.hash(request.password()))  // ← instance method
                .build();

        userRepository.save(user);
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không chính xác."));

        if (!passwordUtil.verify(request.password(), user.getPassword())) {  // ← instance method
            throw new BadRequestException("Email hoặc mật khẩu không chính xác.");
        }

        return buildLoginResponse(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequestDto request) {
        String token = request.getToken();

        if (jwtProvider.isTokenExpired(token)) {
            throw new VsException(HttpStatus.UNAUTHORIZED, ErrorMessage.Auth.ERR_TOKEN_INVALIDATED);
        }

        String jwtId = jwtProvider.extractTokenId(token);

        if (invalidatedTokenRepository.existsById(jwtId)) {
            throw new VsException(HttpStatus.BAD_REQUEST, ErrorMessage.Auth.ERR_TOKEN_ALREADY_INVALIDATED);
        }

        Date expirationDate = jwtProvider.extractExpiration(token);
        LocalDateTime expirationTime = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        invalidatedTokenRepository.save(new InvalidatedToken(jwtId, expirationTime));
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
    public void resetPassword(ResetPasswordRequestDto request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp.");
        }

        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.FORGOT_PASSWORD);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.email()));

        user.setPassword(passwordUtil.hash(request.newPassword()));  // ← instance method
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