package com.example.lockly.domain.dto.request;

public record SendImageMessageSocketRequestDto(
        String conversationId,
        String imageUrl
) {
}