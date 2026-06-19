package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.Post;

public record PostResponseDto(
        UserResponseDto user,
        String caption,
        String imageUrl,
        String contentType
) {
    public static PostResponseDto from(Post posts, String imageUrl){
        return new PostResponseDto(
                UserResponseDto.from(posts.getUser()),
                posts.getCaption(),
                imageUrl,
                posts.getContentType()
        );
    }
}
