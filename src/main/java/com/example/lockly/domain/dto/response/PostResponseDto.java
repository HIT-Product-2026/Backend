package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.PostModeLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostResponseDto {

    private String id;
    private UserResponseDto user;
    private String caption;
    private String imageUrl;
    private String contentType;
    private Double latitude;
    private Double longitude;
    private PostModeLocation modeLocation;

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