package com.example.lockly.mapper.post;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.mapper.user.UserResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostResponseMapper {

    private final UserResponseMapper userResponseMapper;

    public PostResponseDto from(
            Post post,
            String imageUrl,
            Double latitude,
            Double longitude
    ) {
        boolean isPublic = post.getModeLocation() == PostModeLocation.PUBLIC;

        return new PostResponseDto(
                post.getId(),
                userResponseMapper.from(post.getUser()),
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

    public PostResponseDto from(Post post, String urlImage) {
        boolean isPublic = post.getModeLocation() == PostModeLocation.PUBLIC;

        return new PostResponseDto(
                post.getId(),
                userResponseMapper.from(post.getUser()),
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

}
