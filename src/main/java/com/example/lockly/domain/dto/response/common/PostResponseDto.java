package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.PostModeLocation;

import java.util.UUID;

public record PostResponseDto (

    UUID id,
    UserResponseDto user,
    String caption,
    String imageUrl,
    String contentType,
    Double latitude,
    Double longitude,
    PostModeLocation modeLocation
){

    public static PostResponseDto from(Post post, String imageUrl, Double latitude, Double longitude) {
        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                imageUrl,
                post.getContentType(),
                latitude,
                longitude,
                post.getModeLocation()
        );
    }

    public static PostResponseDto from(Post post, String imageUrl) {
        return new PostResponseDto(
                post.getId(),
                UserResponseDto.from(post.getUser()),
                post.getCaption(),
                imageUrl,
                post.getContentType(),
                post.getLatitude(),
                post.getLongitude(),
                post.getModeLocation()
        );
    }
}