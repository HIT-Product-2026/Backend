package com.example.lockly.service;

import com.example.lockly.domain.dto.request.auth.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.User;

import java.util.UUID;

public interface AuthService {
    void sendOtpForRegister(RegisterRequestDto request);
    UserResponseDto verifyOtpAndRegister(VerifyOtpRequestDto request);
    UUID getCurrentUserId();
    LoginResponseDto login(LoginRequestDto request);
    void logout(LogoutRequestDto request);
    void sendOtpForForgotPassword(ForgotPasswordRequestDto request);
    void verifyOtpForgotPassword(VerifyOtpRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);
    User getCurrentUser();
    LoginResponseDto refreshToken(RefreshTokenRequestDto refreshToken);
    User getUserFromCache();
}