package com.example.lockly.domain.dto.request;

public record RegisterPendingData(
        String email,
        String passwordHash
) {}