package com.example.lockly.domain.dto.request.create;

import com.example.lockly.domain.entity.main.enumEntity.Emoji;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReactEmojiToPostRequestDto(

        @NotNull
        UUID postId,

        @NotNull
        Emoji emoji
) {
}