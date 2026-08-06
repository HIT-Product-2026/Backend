package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.mapper.user.UserResponseMapper;

import java.util.UUID;

public record ConversationSimpleResponseDto(
        UUID id,
        UserResponseDto user1,
        UserResponseDto user2
) {
}
