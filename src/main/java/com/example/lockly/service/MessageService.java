package com.example.lockly.service;

import com.example.lockly.domain.dto.request.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.MessageResponseDto;

import java.util.List;

public interface MessageService {
    public MessageResponseDto sendTextMessage(SendTextMessageRequestDto request);
    public MessageResponseDto sendImageMessage(SendImageMessageRequestDto request) throws Exception;
        public List<MessageResponseDto> getMessages(String conversationId);
    public MessageResponseDto getMessageById(String id);
}
