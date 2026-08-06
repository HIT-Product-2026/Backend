package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.main.Friendship;
import com.example.lockly.mapper.UserResponseMapper;

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
                UserResponseMapper.from(friendship.getRequester()),
                UserResponseMapper.from(friendship.getReceiver()),
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getUpdatedAt()
        );
    }
}
