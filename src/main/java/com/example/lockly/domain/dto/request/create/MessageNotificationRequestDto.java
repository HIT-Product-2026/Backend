package com.example.lockly.domain.dto.request.create;

import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.enumEntity.MessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageNotificationRequestDto(
        UUID conversationId,
        UserSimpleResponseDto sender,
        String lastMessageContent,
        MessageType type,
        String imageUrl,
        LocalDateTime lastMessageTime
) {
    public static MessageNotificationRequestDto from(
            Conversation conversation,
            UserSimpleResponseDto sender
    ){
        return new MessageNotificationRequestDto(
                conversation.getId(),
                sender,
                conversation.getLastMessageContent(),
                MessageType.TEXT,
                null,
                conversation.getLastMessageTime()
        );
    }

    public static MessageNotificationRequestDto from(
            Conversation conversation,
            UserSimpleResponseDto sender,
            String imageUrl
    ){
        return new MessageNotificationRequestDto(
                conversation.getId(),
                sender,
                conversation.getLastMessageContent(),
                MessageType.IMAGE,
                imageUrl,
                conversation.getLastMessageTime()
        );
    }
}
