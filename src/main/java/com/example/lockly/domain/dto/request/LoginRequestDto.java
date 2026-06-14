package com.example.lockly.domain.dto.request;

public record LoginRequestDto(
        String username,
        String password,
        String email
) {
}

