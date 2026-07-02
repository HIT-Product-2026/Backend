package com.example.lockly.domain.dto.request;

import java.util.List;
import java.util.UUID;

public record GetEmojiPostsRequestDto(
        List<UUID> postIds
) {}
