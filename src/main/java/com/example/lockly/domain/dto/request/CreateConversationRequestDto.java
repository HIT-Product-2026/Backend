package com.example.lockly.domain.dto.request;

public record CreateConversationRequestDto(
        String userId1,
        String userId2
) {
}
