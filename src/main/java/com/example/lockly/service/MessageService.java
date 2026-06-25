package com.example.lockly.service;

import com.example.lockly.domain.dto.request.SendImageMessageRequestDto;
import com.example.lockly.domain.dto.request.SendTextMessageRequestDto;
import com.example.lockly.domain.dto.response.common.MessageResponseDto;

import java.io.InputStream;
import java.util.List;

public interface MessageService {
    public MessageResponseDto sendTextMessage(SendTextMessageRequestDto request);
    public MessageResponseDto sendImageMessage(SendImageMessageRequestDto request) throws Exception;
    public List<MessageResponseDto> findMessagesByConversationId(String conversationId);
    public MessageResponseDto findMessageById(String id);
    public InputStream findImageMessageById(String id) throws Exception;
}
