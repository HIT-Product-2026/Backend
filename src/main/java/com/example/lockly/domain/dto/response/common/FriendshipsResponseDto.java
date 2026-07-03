package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendship;

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
    public static <T> FriendshipsResponseDto from(Friendship friendship){
        return new FriendshipsResponseDto(
                friendship.getId(),
                UserResponseDto.from(friendship.getRequester()),
                UserResponseDto.from(friendship.getReceiver()),
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getUpdatedAt()
        );
    }
}
