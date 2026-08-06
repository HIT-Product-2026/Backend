package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.mapper.user.UserSimpleResponseMapper;

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
                UserSimpleResponseMapper.from(conversation.getUser1()),
                UserSimpleResponseMapper.from(conversation.getUser2()),
                conversation.getLastMessageContent(),
                conversation.getLastMessageTime()
        );
    }
}
