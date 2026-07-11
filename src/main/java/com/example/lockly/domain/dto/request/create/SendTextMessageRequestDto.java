package com.example.lockly.domain.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendTextMessageRequestDto(

        @NotNull
        UUID conversationId,

        @NotBlank
        String content
) {
}