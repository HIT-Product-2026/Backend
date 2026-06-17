package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendships;

import java.time.LocalDateTime;

public record FriendshipsResponseDto(
        String id,
        UserResponseDto requester,
        UserResponseDto receiver,
        FriendshipStatus status,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
    public static <T> FriendshipsResponseDto from(Friendships friendships){
        return new FriendshipsResponseDto(
                friendships.getId(),
                UserResponseDto.from(friendships.getRequester()),
                UserResponseDto.from(friendships.getReceiver()),
                friendships.getStatus(),
                friendships.getCreateAt(),
                friendships.getUpdateAt()
        );
    }
}
