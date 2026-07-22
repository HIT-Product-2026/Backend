package com.example.lockly.domain.dto.request.auth;

import lombok.Builder;

@Builder
public record ForgotPasswordCacheDto(
        String otp
) {
}
