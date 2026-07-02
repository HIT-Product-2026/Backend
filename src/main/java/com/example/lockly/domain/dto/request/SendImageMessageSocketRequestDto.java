package com.example.lockly.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendImageMessageSocketRequestDto(

        @NotNull
        UUID conversationId,

        @NotBlank
        String imageUrl
) {
}