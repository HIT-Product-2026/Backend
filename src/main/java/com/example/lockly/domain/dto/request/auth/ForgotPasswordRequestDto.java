package com.example.lockly.domain.dto.request.auth;

import com.example.lockly.common.validator.ValidEmail;

public record ForgotPasswordRequestDto(

        @ValidEmail
        String email

) {}