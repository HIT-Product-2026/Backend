package com.example.lockly.domain.dto.request.create;

import java.util.UUID;

public record CreateConversationRequestDto(
        UUID userId1,
        UUID userId2
) {
}
