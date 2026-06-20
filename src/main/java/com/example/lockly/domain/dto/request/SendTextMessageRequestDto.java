package com.example.lockly.domain.dto.request;

public record SendTextMessageRequestDto(
        String conversationId,
        String senderId,
        String content
) {
}
