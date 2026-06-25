package com.example.lockly.domain.dto.request;

import java.io.Serializable;

public record RegisterPendingData(
        String email,
        String passwordHash
) implements Serializable {}