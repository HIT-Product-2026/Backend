package com.example.lockly.mapper.post;

import com.example.lockly.domain.dto.response.common.ConversationSimpleResponseDto;
import com.example.lockly.domain.dto.response.common.PostDetailResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.mapper.conversation.ConversationSimpleResponseMapper;
import com.example.lockly.mapper.user.UserResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostDetailResponseMapper {

    private final UserResponseMapper userResponseMapper;
    private final ConversationSimpleResponseMapper conversationSimpleResponseMapper;

    public PostDetailResponseDto from(
            Post post,
            Conversation conversation,
            String urlImage,
            String locationName
    ) {
        boolean isPublic = post.getModeLocation() == PostModeLocation.PUBLIC;

        return new PostDetailResponseDto(
                post.getId(),
                userResponseMapper.from(post.getUser()),
                post.getCaption(),
                isPublic ? post.getLatitude() : null,
                isPublic ? post.getLongitude() : null,
                post.getModeLocation(),
                post.getNsfw(),
                urlImage,
                conversationSimpleResponseMapper.from(conversation),
                post.getCreatedAt(),
                isPublic ? locationName : null,
                post.getType()
        );
    }
}
