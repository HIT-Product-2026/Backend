package com.example.lockly.domain.dto.request.auth;

import com.example.lockly.common.validator.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequestDto(
        @ValidEmail
        String email,

        @NotBlank(message = "OTP không được để trống")
        @Size(min = 6, max = 6, message = "OTP phải đúng 6 chữ số")
        String otp
) {
}
