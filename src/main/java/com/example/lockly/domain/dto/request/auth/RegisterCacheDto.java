package com.example.lockly.domain.dto.request.auth;

import lombok.Builder;

@Builder
public record RegisterCacheDto(
        String otp,
        String passwordHash
) {
}