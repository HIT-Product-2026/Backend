package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.mapper.UserResponseMapper;

import java.util.UUID;

public record ConversationSimpleResponseDto(
        UUID id,
        UserResponseDto user1,
        UserResponseDto user2
) {
    public static ConversationSimpleResponseDto from(Conversation conversation){
        if (conversation == null) {
            return null;
        }

        return new ConversationSimpleResponseDto(
                conversation.getId(),
                UserResponseMapper.from(conversation.getUser1()),
                UserResponseMapper.from(conversation.getUser2())
        );
    }
}
