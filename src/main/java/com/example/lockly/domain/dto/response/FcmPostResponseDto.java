package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.FcmMessageType;

import java.util.UUID;

public record FcmPostResponseDto(
        UUID senderId,
        UUID postId,
        FcmMessageType type
) {
}
