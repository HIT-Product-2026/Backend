package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.Emoji;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReactEmojiToPostRequestDto(

        @NotNull
        UUID postId,

        @NotNull
        Emoji emoji
) {
}