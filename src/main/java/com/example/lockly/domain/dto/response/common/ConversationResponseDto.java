package com.example.lockly.domain.dto.response.common;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponseDto(
        UUID id,
        UserResponseDto user1,
        UserResponseDto user2,
        String lastMessageContent,
        LocalDateTime lastMessageTime
) {
}
