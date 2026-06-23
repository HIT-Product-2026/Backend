package com.example.lockly.service;

import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.User;

public interface AuthService {
    void sendOtpForRegister(RegisterRequestDto request);
    UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request);
    LoginResponseDto login(LoginRequestDto request);
    void logout(LogoutRequestDto request);
    void sendOtpForForgotPassword(ForgotPasswordRequestDto request);
    void verifyOtpForgotPassword(VerifyOtpForgotPasswordRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);
    public User getCurrentUser();
}