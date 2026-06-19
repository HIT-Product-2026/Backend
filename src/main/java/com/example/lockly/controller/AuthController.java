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
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;

    @Validated
    @RestController
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequestMapping("/api/v1/auth")
    @Tag(name = "Auth Controller", description = "Đăng ký, Đăng nhập Gmail, Quên mật khẩu")
    public class AuthController {

        AuthService authService;

        @PostMapping("/register/send-otp")
        @Operation(
                summary = "Đăng ký — Bước 1/2",
                description = "Nhập thông tin và gửi OTP về Gmail"
        )
        public ResponseEntity<ApiResponse<Void>> sendOtpForRegister(
                @Valid @RequestBody RegisterRequestDto request
        ) {
            authService.sendOtpForRegister(request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessMessage.Auth.SEND_OTP_SUCCESS));
        }

        @PostMapping("/register/verify-otp")
        @Operation(
                summary = "Đăng ký — Bước 2/2",
                description = "Xác thực OTP và tạo tài khoản"
        )
        public ResponseEntity<ApiResponse<UserResponseDto>> verifyOtpAndRegister(
                @Valid @RequestBody VerifyOtpRegisterRequestDto request
        ) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse
                            .created(SuccessMessage.Auth.REGISTER_SUCCESS, authService.verifyOtpAndRegister(request)));
        }

        @PostMapping("/login")
        @Operation(
                summary = "Đăng nhập",
                description = "Gmail + Password → nhận JWT"
        )
        public ResponseEntity<ApiResponse<LoginResponseDto>> login(
                @Valid @RequestBody LoginRequestDto request
        ) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse
                            .success(SuccessMessage.Auth.LOGIN_SUCCESS, authService.login(request)));
        }

        @PostMapping("/logout")
        @Operation(
                summary = "Đăng xuất",
                description = "Blacklist token hiện tại"
        )
        public ResponseEntity<ApiResponse<Void>> logout(
                @Valid @RequestBody LogoutRequestDto request
        ) {
            authService.logout(request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessMessage.Auth.LOGOUT_SUCCESS));
        }

        @PostMapping("/forgot-password/send-otp")
        @Operation(
                summary = "Quên mật khẩu — Bước 1/2",
                description = "Gửi OTP về Gmail"
        )
        public ResponseEntity<ApiResponse<Void>> sendOtpForForgotPassword(
                @Valid @RequestBody ForgotPasswordRequestDto request
        ) {
            authService.sendOtpForForgotPassword(request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessMessage.Auth.SEND_OTP_SUCCESS));
        }

        @PostMapping("/forgot-password/reset")
        @Operation(
                summary = "Quên mật khẩu — Bước 2/2",
                description = "Xác thực OTP và đặt mật khẩu mới"
        )
        public ResponseEntity<ApiResponse<Void>> resetPassword(
                @Valid @RequestBody ResetPasswordRequestDto request
        ) {
            authService.resetPassword(request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessMessage.Auth.RESET_PASSWORD_SUCCESS));
        }
    }