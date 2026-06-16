package com.example.lockly.service;

import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.CommonResponseDto;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;

public interface AuthService {
    UserResponseDto register(RegisterRequestDto request);
    LoginResponseDto authentication(LoginRequestDto request);
    CommonResponseDto logout(LogoutRequestDto request);

    // ====== OTP: GỬI MÃ ======

    /**
     * Bước 1 - Đăng ký: gửi OTP về số điện thoại.
     * Trả về message xác nhận đã gửi.
     */
    CommonResponseDto sendOtpForRegister(SendOtpRequestDto request);

    /**
     * Bước 1 - Đăng nhập: gửi OTP về số điện thoại.
     * Số điện thoại phải đã có tài khoản.
     */
    CommonResponseDto sendOtpForLogin(SendOtpRequestDto request);

    // ====== OTP: XÁC THỰC & HOÀN TẤT ======

    /**
     * Bước 2 - Đăng ký: xác thực OTP + tạo tài khoản mới.
     * Trả về thông tin user vừa tạo.
     */
    UserResponseDto verifyOtpAndRegister(VerifyOtpRegisterRequestDto request);

    /**
     * Bước 2 - Đăng nhập: xác thực OTP + cấp JWT token.
     * Trả về access token + refresh token.
     */
    LoginResponseDto verifyOtpAndLogin(VerifyOtpLoginRequestDto request);
}