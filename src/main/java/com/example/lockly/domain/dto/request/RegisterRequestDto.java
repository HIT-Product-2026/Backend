package com.example.lockly.domain.dto.request;

import com.example.lockly.common.validator.ValidEmail;
import com.example.lockly.common.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(

        @ValidEmail
        String email,
        @ValidPassword
        String password
) {}