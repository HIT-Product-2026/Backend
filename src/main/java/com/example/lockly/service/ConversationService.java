package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;

import java.util.List;
import java.util.UUID;

public interface ConversationService {
    public List<ConversationResponseDto> findAll();
    public ConversationResponseDto createConversation(CreateConversationRequestDto request);
    public ConversationResponseDto findById(UUID id);
}
