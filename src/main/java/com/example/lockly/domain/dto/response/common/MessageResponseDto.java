package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Message;
import com.example.lockly.domain.entity.main.enumEntity.MessageType;
import com.example.lockly.mapper.user.UserResponseMapper;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponseDto (
    UUID id,
    UserResponseDto sender,
    UUID conversationId,
    String imageUrl,
    String content,
    MessageType type,
    LocalDateTime createdAt,
    Boolean isRead
){
}
