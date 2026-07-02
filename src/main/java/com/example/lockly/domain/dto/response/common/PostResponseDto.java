package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.entity.NsfwStatus;
import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.PostModeLocation;

import java.util.UUID;

public record PostResponseDto (

    UUID id,
    UserResponseDto user,
    String caption,
    Double latitude,
    Double longitude,
    PostModeLocation modeLocation,
    NsfwStatus nsfw
){

    public static PostResponseDto from(Post post, String imageUrl, Double latitude, Double longitude) {
        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                latitude,
                longitude,
                post.getModeLocation(),
                NsfwStatus.PROCESSING
        );
    }

    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                post.getLatitude(),
                post.getLongitude(),
                post.getModeLocation(),
                NsfwStatus.PROCESSING
        );
    }

    public static PostResponseDto from(DetectNsfwPostRequestDto dto, NsfwStatus nsfw){
        return new PostResponseDto(
                dto.postId(),
                dto.user(),
                dto.caption(),
                dto.latitude(),
                dto.longitude(),
                dto.modeLocation(),
                nsfw
        );
    }
}