package com.example.lockly.domain.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
        @NotBlank
        String refeshToken
) {
}
