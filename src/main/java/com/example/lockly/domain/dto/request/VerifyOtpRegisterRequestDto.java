package com.example.lockly.domain.dto.request;

import com.example.lockly.common.validator.ValidEmail;
import com.example.lockly.common.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyOtpRegisterRequestDto(

        @ValidEmail
        String email,

        @NotBlank(message = "OTP không được để trống")
        @Size(min = 6, max = 6, message = "OTP phải đúng 6 chữ số")
        String otp,

        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username từ 3 đến 50 ký tự")
        String username,

        @NotBlank(message = "Tên hiển thị không được để trống")
        @Size(min = 2, max = 100, message = "Tên hiển thị từ 2 đến 100 ký tự")
        String displayName,

        @ValidPassword
        String password,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword

) {}