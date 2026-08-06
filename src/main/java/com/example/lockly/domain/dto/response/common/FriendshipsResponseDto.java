package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.main.Friendship;
import com.example.lockly.mapper.user.UserResponseMapper;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendshipsResponseDto(
        UUID id,
        UserResponseDto requester,
        UserResponseDto receiver,
        FriendshipStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
