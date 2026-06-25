package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.Emoji;

public record ReactEmojiToPostRequestDto(
        String postId,
        Emoji emoji
) {
}
