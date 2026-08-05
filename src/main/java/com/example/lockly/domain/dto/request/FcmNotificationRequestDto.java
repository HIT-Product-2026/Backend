package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.FcmMessageType;
import com.example.lockly.domain.entity.main.enumEntity.TypePost;

import java.util.List;
import java.util.UUID;

public record FcmNotificationRequestDto(

        UUID senderId,
        UUID postId,
        FcmMessageType type,
        List<String> fcmToken,
        String displayName,
        String avatarUrl,
        String imageUrl,
        Double latitude,
        Double longitude,
        String caption,
        TypePost typePost
) {
    public static FcmNotificationRequestDto from(
            User user,
            PostResponseDto post,
            List<String> fcmTokens,
            String avatarUrl,
            String imageUrl,
            Double latitude,
            Double longitude
    ){
        return new FcmNotificationRequestDto(
                user.getId(),
                post.id(),
                FcmMessageType.POST,
                fcmTokens,
                user.getDisplayName(),
                avatarUrl,
                imageUrl,
                latitude,
                longitude,
                post.caption(),
                post.type()
        );
    }
}