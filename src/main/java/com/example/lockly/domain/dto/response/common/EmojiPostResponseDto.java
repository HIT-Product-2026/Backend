package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.Emoji;
import com.example.lockly.domain.entity.EmojiPost;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmojiPostResponseDto(
        UUID emojiId,
        UUID postId,
        UserSimpleResponseDto sender,
        Emoji emoji,
        LocalDateTime createdAt
) {
    public static EmojiPostResponseDto from(EmojiPost emojiPost){
        return new EmojiPostResponseDto(
                emojiPost.getId(),
                emojiPost.getPost().getId(),
                UserSimpleResponseDto.from(emojiPost.getSender()),
                emojiPost.getEmoji(),
                emojiPost.getCreatedAt()
        );
    }
}
