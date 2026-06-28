package com.example.lockly.domain.dto.request;

import java.util.UUID;

public record SendImageMessageSocketRequestDto(
        UUID conversationId,
        String imageUrl
) {
}