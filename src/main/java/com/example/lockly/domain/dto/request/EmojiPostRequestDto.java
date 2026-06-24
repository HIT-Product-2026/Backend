package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.Emoji;

public record EmojiPostRequestDto(
        String postId,
        Emoji emoji
) {
}
