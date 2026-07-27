package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.entity.main.enumEntity.NsfwStatus;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;

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
    LocalDateTime createAt
){

    public static PostResponseDto from(Post post, String imageUrl, Double latitude, Double longitude) {
        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                latitude,
                longitude,
                post.getModeLocation(),
                NsfwStatus.PROCESSING,
                imageUrl,
                post.getObjectName(),
                post.getCreatedAt()
        );
    }

    public static PostResponseDto from(Post post, String urlImage) {
        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                post.getLatitude(),
                post.getLongitude(),
                post.getModeLocation(),
                NsfwStatus.PROCESSING,
                urlImage,
                post.getObjectName(),
                post.getCreatedAt()
        );
    }

    public static PostResponseDto from(DetectNsfwPostRequestDto dto, NsfwStatus nsfw, String urlImage){
        return new PostResponseDto(
                dto.postId(),
                dto.user(),
                dto.caption(),
                dto.latitude(),
                dto.longitude(),
                dto.modeLocation(),
                nsfw,
                urlImage,
                dto.objectName(),
                dto.createAt()
        );
    }
}