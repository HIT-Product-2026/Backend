package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.enumEntity.NsfwStatus;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.domain.entity.main.enumEntity.TypePost;
import com.example.lockly.mapper.user.UserResponseMapper;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDetailResponseDto(
        UUID id,
        UserResponseDto user,
        String caption,
        Double latitude,
        Double longitude,
        PostModeLocation modeLocation,
        NsfwStatus nsfw,
        String urlImage,
        ConversationSimpleResponseDto conversation,
        LocalDateTime createAt,
        String locationName,
        TypePost type
){
}
