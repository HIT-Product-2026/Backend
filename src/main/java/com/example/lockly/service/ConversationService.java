package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.domain.entity.main.User;

import java.util.List;
import java.util.UUID;

public interface ConversationService {
    List<ConversationResponseDto> findAll();
    ConversationResponseDto createConversation(CreateConversationRequestDto request);
    ConversationResponseDto findById(UUID id);
    User getOtherUser(UUID conversationId, UUID currentUserId);
}
