package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.FriendshipStatus;

public record FriendshipsRequestDto(
        String id,
        UserResponseDto requester,
        UserResponseDto receiver,
        FriendshipStatus status
) {

}
