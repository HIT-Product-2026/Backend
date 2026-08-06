package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponseDto(
    UUID id,
    String username,
    String displayName,
    UserMode mode,
    String avatarUrl
){ }
