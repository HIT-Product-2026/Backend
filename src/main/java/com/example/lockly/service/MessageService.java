package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.create.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.User;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponseDto sendTextMessage(SendTextMessageRequestDto request, User user);
    MessageResponseDto sendImageMessage(SendImageMessageRequestDto request, User user);
    List<MessageResponseDto> findMessagesByConversationId(UUID conversationId);
    MessageResponseDto findMessageById(UUID id);
    InputStream findImageMessageById(UUID id);
}
