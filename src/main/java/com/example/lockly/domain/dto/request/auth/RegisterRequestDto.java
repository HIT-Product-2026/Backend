package com.example.lockly.domain.dto.request.auth;

import com.example.lockly.common.validator.ValidEmail;
import com.example.lockly.common.validator.ValidPassword;

public record RegisterRequestDto(

        @ValidEmail
        String email,
        @ValidPassword
        String password
) {}