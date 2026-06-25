package com.example.lockly.domain.dto.request.auth;

import com.example.lockly.common.validator.ValidEmail;
import com.example.lockly.common.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(

        @ValidEmail
        String email,

        @ValidPassword
        String newPassword,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword

) {}