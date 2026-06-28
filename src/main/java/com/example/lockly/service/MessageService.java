package com.example.lockly.service;

import com.example.lockly.domain.dto.request.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    public MessageResponseDto sendTextMessage(SendTextMessageRequestDto request);
    public MessageResponseDto sendImageMessage(SendImageMessageRequestDto request) throws Exception;
public List<MessageResponseDto> findMessagesByConversationId(UUID conversationId);
    public MessageResponseDto findMessageById(UUID id);
    public InputStream findImageMessageById(UUID id) throws Exception;
}
