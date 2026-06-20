package com.example.lockly.domain.dto.request;

import com.example.lockly.common.validator.ValidEmail;

public record ForgotPasswordRequestDto(

        @ValidEmail
        String email

) {}