package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.Emoji;

import java.util.UUID;

public record ReactEmojiToPostRequestDto(
        UUID postId,
        Emoji emoji
) {
}
