package com.example.lockly.service;

import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;

public interface AuthService {
    void sendOtpForRegister(RegisterRequestDto request);
    UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request);
    LoginResponseDto login(LoginRequestDto request);
    void logout(LogoutRequestDto request);   // ← đổi CommonResponseDto → void
    void sendOtpForForgotPassword(ForgotPasswordRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);
}