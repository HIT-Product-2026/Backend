package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendship;

import java.time.LocalDateTime;

public record FriendshipsResponseDto(
        String id,
        UserResponseDto requester,
        UserResponseDto receiver,
        FriendshipStatus status,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
    public static <T> FriendshipsResponseDto from(Friendship friendship){
        return new FriendshipsResponseDto(
                friendship.getId(),
                UserResponseDto.from(friendship.getRequester()),
                UserResponseDto.from(friendship.getReceiver()),
                friendship.getStatus(),
                friendship.getCreateAt(),
                friendship.getUpdateAt()
        );
    }
}
