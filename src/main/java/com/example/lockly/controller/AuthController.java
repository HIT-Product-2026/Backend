package com.example.lockly.controller;

import com.example.lockly.common.ApiResponse;
import com.example.lockly.constant.SuccessMessage;
import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Auth", description = "Đăng ký, Đăng nhập Gmail, Quên mật khẩu")
public class AuthController {

    AuthService authService;

    /** BƯỚC 1/2 — Đăng ký */
    @Operation(summary = "[Đăng ký] Bước 1/2 — Nhập thông tin và gửi OTP về Gmail")
    @PostMapping("/register/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtpForRegister(
            @Valid @RequestBody RegisterRequestDto request) {

        authService.sendOtpForRegister(request);
        return ResponseEntity.ok(ApiResponse.ok(200, SuccessMessage.Auth.SEND_OTP_SUCCESS));
    }

    /** BƯỚC 2/2 — Đăng ký */
    @Operation(summary = "[Đăng ký] Bước 2/2 — Xác thực OTP và tạo tài khoản")
    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<UserResponseDto>> verifyOtpAndRegister(
            @Valid @RequestBody VerifyOtpRegisterRequestDto request) {

        UserResponseDto data = authService.verifyOtpAndRegister(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(201, SuccessMessage.Auth.REGISTER_SUCCESS, data));
    }

    /** POST /api/v1/auth/login */
    @Operation(summary = "[Đăng nhập] Gmail + Password → nhận JWT")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {

        LoginResponseDto data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(200, SuccessMessage.Auth.LOGIN_SUCCESS, data));
    }

    /** POST /api/v1/auth/logout */
    @Operation(summary = "Đăng xuất")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequestDto request) {

        authService.logout(request);  // ← giờ trả void, controller tự build response
        return ResponseEntity.ok(ApiResponse.ok(200, SuccessMessage.Auth.LOGOUT_SUCCESS));
    }

    /** BƯỚC 1/2 — Quên mật khẩu */
    @Operation(summary = "[Quên MK] Bước 1/2 — Gửi OTP về Gmail")
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtpForForgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {

        authService.sendOtpForForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(200, SuccessMessage.Auth.SEND_OTP_SUCCESS));
    }

    /** BƯỚC 2/2 — Quên mật khẩu */
    @Operation(summary = "[Quên MK] Bước 2/2 — Xác thực OTP và đặt mật khẩu mới")
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(200, SuccessMessage.Auth.RESET_PASSWORD_SUCCESS));
    }
}