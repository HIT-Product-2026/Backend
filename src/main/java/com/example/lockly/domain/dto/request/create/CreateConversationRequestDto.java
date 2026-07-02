package com.example.lockly.domain.dto.request.create;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateConversationRequestDto(

        @NotNull
        UUID userId1,

        @NotNull
        UUID userId2
) {
}