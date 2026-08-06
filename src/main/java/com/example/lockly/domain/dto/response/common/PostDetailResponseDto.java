package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.enumEntity.NsfwStatus;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.domain.entity.main.enumEntity.TypePost;
import com.example.lockly.mapper.UserResponseMapper;

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
    public static PostDetailResponseDto from(
            Post post,
            Conversation conversation,
            String urlImage,
            String locationName
    ) {
        boolean isPublic = post.getModeLocation() == PostModeLocation.PUBLIC;

        return new PostDetailResponseDto(
                post.getId(),
                UserResponseMapper.from(post.getUser()),
                post.getCaption(),
                isPublic ? post.getLatitude() : null,
                isPublic ? post.getLongitude() : null,
                post.getModeLocation(),
                post.getNsfw(),
                urlImage,
                ConversationSimpleResponseDto.from(conversation),
                post.getCreatedAt(),
                isPublic ? locationName : null,
                post.getType()
        );
    }
}
