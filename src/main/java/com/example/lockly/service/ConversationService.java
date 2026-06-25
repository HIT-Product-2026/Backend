package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;

import java.util.List;

public interface ConversationService {
    public List<ConversationResponseDto> findAll();
    public ConversationResponseDto createConversation(CreateConversationRequestDto request);
    public ConversationResponseDto findById(String id);
}
