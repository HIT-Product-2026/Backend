package com.example.lockly.controller;

import com.example.lockly.common.ApiResponse;
import com.example.lockly.domain.dto.request.*;
import com.example.lockly.domain.dto.response.CommonResponseDto;
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
@Tag(name = "Auth", description = "API Xác thực - Đăng ký, Đăng nhập, OTP")
public class AuthController {

    AuthService authService;

    // =============================================
    // ĐĂNG KÝ / ĐĂNG NHẬP CŨ (Username + Password)
    // =============================================

    @Operation(summary = "Đăng ký tài khoản (Username + Password)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {

        UserResponseDto user = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(201, "Đăng ký thành công", user));
    }

    @Operation(summary = "Đăng nhập (Username + Password)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {

        LoginResponseDto token = authService.authentication(request);
        return ResponseEntity.ok(ApiResponse.ok(200, "Đăng nhập thành công", token));
    }

    @Operation(summary = "Đăng xuất")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequestDto request) {

        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok(200, "Đăng xuất thành công"));
    }

    // =============================================
    // ĐĂNG KÝ BẰNG OTP (2 bước)
    // =============================================

    /**
     * BƯỚC 1/2 - Đăng ký:
     * Client gửi số điện thoại -> BE gửi OTP qua Zalo Bot.
     * <p>
     * Request:  POST /api/v1/auth/otp/register/send
     * Body:     { "phoneNumber": "0912345678" }
     * Response: 200 OK + message xác nhận
     */
    @Operation(summary = "[OTP] Bước 1: Gửi mã OTP để đăng ký")
    @PostMapping("/otp/register/send")
    public ResponseEntity<ApiResponse<Void>> sendOtpForRegister(
            @Valid @RequestBody SendOtpRequestDto request) {

        authService.sendOtpForRegister(request);
        return ResponseEntity.ok(
                ApiResponse.ok(200, "Mã OTP đã được gửi qua Zalo. Vui lòng nhập mã trong vòng 5 phút.")
        );
    }

    /**
     * BƯỚC 2/2 - Đăng ký:
     * Client gửi SĐT + OTP + thông tin tài khoản -> BE xác thực OTP và tạo user.
     * <p>
     * Request:  POST /api/v1/auth/otp/register/verify
     * Body:     { "phoneNumber": "...", "otp": "123456", "username": "...", "displayName": "..." }
     * Response: 201 CREATED + thông tin user
     */
    @Operation(summary = "[OTP] Bước 2: Xác thực OTP và hoàn tất đăng ký")
    @PostMapping("/otp/register/verify")
    public ResponseEntity<ApiResponse<UserResponseDto>> verifyOtpAndRegister(
            @Valid @RequestBody VerifyOtpRegisterRequestDto request) {

        UserResponseDto user = authService.verifyOtpAndRegister(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(201, "Đăng ký tài khoản thành công!", user));
    }

    // =============================================
    // ĐĂNG NHẬP BẰNG OTP (2 bước)
    // =============================================

    /**
     * BƯỚC 1/2 - Đăng nhập:
     * Client gửi số điện thoại -> BE gửi OTP qua Zalo Bot.
     * <p>
     * Request:  POST /api/v1/auth/otp/login/send
     * Body:     { "phoneNumber": "0912345678" }
     * Response: 200 OK + message xác nhận
     */
    @Operation(summary = "[OTP] Bước 1: Gửi mã OTP để đăng nhập")
    @PostMapping("/otp/login/send")
    public ResponseEntity<ApiResponse<Void>> sendOtpForLogin(
            @Valid @RequestBody SendOtpRequestDto request) {

        authService.sendOtpForLogin(request);
        return ResponseEntity.ok(
                ApiResponse.ok(200, "Mã OTP đã được gửi qua Zalo. Vui lòng nhập mã trong vòng 5 phút.")
        );
    }

    /**
     * BƯỚC 2/2 - Đăng nhập:
     * Client gửi SĐT + OTP -> BE xác thực và cấp JWT token.
     * <p>
     * Request:  POST /api/v1/auth/otp/login/verify
     * Body:     { "phoneNumber": "0912345678", "otp": "123456" }
     * Response: 200 OK + { accessToken, refreshToken, userId, tokenType }
     */
    @Operation(summary = "[OTP] Bước 2: Xác thực OTP và lấy JWT token")
    @PostMapping("/otp/login/verify")
    public ResponseEntity<ApiResponse<LoginResponseDto>> verifyOtpAndLogin(
            @Valid @RequestBody VerifyOtpLoginRequestDto request) {

        LoginResponseDto token = authService.verifyOtpAndLogin(request);
        return ResponseEntity.ok(ApiResponse.ok(200, "Đăng nhập thành công!", token));
    }
}