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

        @ValidPassword
        String password,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword

) {}