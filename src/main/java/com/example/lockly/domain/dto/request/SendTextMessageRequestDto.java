package com.example.lockly.domain.dto.request;

import java.util.UUID;

public record SendTextMessageRequestDto(
        UUID conversationId,
        String content
) {
}
