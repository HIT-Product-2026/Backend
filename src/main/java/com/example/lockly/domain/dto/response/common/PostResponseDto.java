package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.entity.main.enumEntity.NsfwStatus;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.domain.entity.main.enumEntity.TypePost;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponseDto (

    UUID id,
    UserResponseDto user,
    String caption,
    Double latitude,
    Double longitude,
    PostModeLocation modeLocation,
    NsfwStatus nsfw,
    String urlImage,
    String objectName,
    LocalDateTime createAt,
    TypePost type
){

    public static PostResponseDto from(
            Post post,
            String imageUrl,
            Double latitude,
            Double longitude
    ) {
        boolean isPublic = post.getModeLocation() == PostModeLocation.PUBLIC;

        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                isPublic ? latitude : null,
                isPublic ? longitude : null,
                post.getModeLocation(),
                post.getNsfw(),
                imageUrl,
                post.getObjectName(),
                post.getCreatedAt(),
                post.getType()
        );
    }

    public static PostResponseDto from(Post post, String urlImage) {
        boolean isPublic = post.getModeLocation() == PostModeLocation.PUBLIC;

        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                isPublic ? post.getLatitude() : null,
                isPublic ? post.getLongitude() : null,
                post.getModeLocation(),
                post.getNsfw(),
                urlImage,
                post.getObjectName(),
                post.getCreatedAt(),
                post.getType()
        );
    }

    public static PostResponseDto from(DetectNsfwPostRequestDto dto, NsfwStatus nsfw, String urlImage){
        boolean isPublic = dto.modeLocation() == PostModeLocation.PUBLIC;

        return new PostResponseDto(
                dto.postId(),
                dto.user(),
                dto.caption(),
                isPublic ? dto.latitude() : null,
                isPublic ? dto.longitude() : null,
                dto.modeLocation(),
                nsfw,
                urlImage,
                dto.objectName(),
                dto.createAt(),
                TypePost.IMAGE
        );
    }
}