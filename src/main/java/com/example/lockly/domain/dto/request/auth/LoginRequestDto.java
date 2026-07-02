package com.example.lockly.domain.dto.request.auth;

import com.example.lockly.common.validator.ValidEmail;
import com.example.lockly.common.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(

        @ValidEmail
        String email,

        @ValidPassword
        String password
) {}