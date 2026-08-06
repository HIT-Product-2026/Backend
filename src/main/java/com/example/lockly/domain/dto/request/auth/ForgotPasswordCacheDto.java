package com.example.lockly.domain.dto.request.auth;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ForgotPasswordCacheDto(
        String otp
) implements Serializable {
}
