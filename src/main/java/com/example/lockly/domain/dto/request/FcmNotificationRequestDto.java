package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.FcmMessageType;

import java.util.List;
import java.util.UUID;

public record FcmNotificationRequestDto(
    UUID senderId,
    UUID postId,
    FcmMessageType type,
    List<String> fcmToken
){
    public static FcmNotificationRequestDto from(UUID senderId, UUID postId, List<String> fcmTokens){
        return new FcmNotificationRequestDto(
                senderId,
                postId,
                FcmMessageType.POST,
                fcmTokens
        );
    }
}
