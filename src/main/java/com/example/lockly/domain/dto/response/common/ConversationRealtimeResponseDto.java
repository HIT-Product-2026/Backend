package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Conversation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationRealtimeResponseDto(
        UUID id,
        UserSimpleResponseDto user1,
        UserSimpleResponseDto user2,
        String lastMessageContent,
        LocalDateTime lastMessageTime
) {
    public static ConversationRealtimeResponseDto from(Conversation conversation){
        return new ConversationRealtimeResponseDto(
                conversation.getId(),
                UserSimpleResponseDto.from(conversation.getUser1()),
                UserSimpleResponseDto.from(conversation.getUser2()),
                conversation.getLastMessageContent(),
                conversation.getLastMessageTime()
        );
    }
}
