package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.Message;
import com.example.lockly.domain.entity.MessageType;

import java.time.LocalDateTime;

public record MessageResponseDto(
        UserResponseDto sender,
        String content,
        MessageType type,
        LocalDateTime createdAt
) {
    public static MessageResponseDto from(Message message){
        return new MessageResponseDto(
                UserResponseDto.from(message.getSender()),
                message.getContent(),
                message.getType(),
                message.getCreatedAt()
        );
    }
}
