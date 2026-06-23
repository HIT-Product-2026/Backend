package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.FcmMessageType;

public record FcmPostResponseDto(
        String senderId,
        String postId,
        FcmMessageType type
) {
}
