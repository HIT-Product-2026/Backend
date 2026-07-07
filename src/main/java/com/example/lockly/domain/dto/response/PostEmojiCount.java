package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.main.Post;

public record PostEmojiCount(
        Post post,
        long emojiCount
) {}
