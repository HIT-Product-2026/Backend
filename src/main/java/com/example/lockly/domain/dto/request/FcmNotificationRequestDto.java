package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.FcmMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record FcmNotificationRequestDto(

        @NotNull
        UUID senderId,

        @NotNull
        UUID postId,

        @NotNull
        FcmMessageType type,

        @NotEmpty
        @Size(max = 500)
        List<@NotBlank String> fcmToken
) {
    public static FcmNotificationRequestDto from(UUID senderId, UUID postId, List<String> fcmTokens){
        return new FcmNotificationRequestDto(
                senderId,
                postId,
                FcmMessageType.POST,
                fcmTokens
        );
    }
}