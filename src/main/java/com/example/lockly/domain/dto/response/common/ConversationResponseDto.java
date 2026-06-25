package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.Conversation;

import java.time.LocalDateTime;

public record ConversationResponseDto(
        UserResponseDto user1,
        UserResponseDto user2,
        String lastMessageContent,
        LocalDateTime lastMessageTime
) {
    public static ConversationResponseDto from(Conversation conversation){
        return new ConversationResponseDto(
                UserResponseDto.from(conversation.getUser1()),
                UserResponseDto.from(conversation.getUser2()),
                conversation.getLastMessageContent(),
                conversation.getLastMessageTime()
        );
    }
}
