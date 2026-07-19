package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Conversation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponseDto(
        UUID id,
        UserResponseDto user1,
        UserResponseDto user2,
        String lastMessageContent,
        LocalDateTime lastMessageTime
) {
    public static ConversationResponseDto from(Conversation conversation){
        return new ConversationResponseDto(
                conversation.getId(),
                UserResponseDto.from(conversation.getUser1()),
                UserResponseDto.from(conversation.getUser2()),
                conversation.getLastMessageContent(),
                conversation.getLastMessageTime()
        );
    }
}
