package com.example.lockly.service;

import com.example.lockly.domain.dto.request.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.ConversationResponseDto;

import java.util.List;

public interface ConversationService {
    public List<ConversationResponseDto> findByUser(String userId);
    public ConversationResponseDto createConversation(CreateConversationRequestDto request);
    public ConversationResponseDto getConversationById(String id);
}
