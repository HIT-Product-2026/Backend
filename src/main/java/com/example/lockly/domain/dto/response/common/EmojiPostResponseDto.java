package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.enumEntity.Emoji;
import com.example.lockly.domain.entity.main.EmojiPost;
import com.example.lockly.mapper.user.UserSimpleResponseMapper;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmojiPostResponseDto(
        UUID emojiId,
        UUID postId,
        UserSimpleResponseDto sender,
        Emoji emoji,
        LocalDateTime createdAt
) {
}
