package com.example.lockly.service;

import com.example.lockly.domain.dto.response.FcmPostResponseDto;

import java.util.List;
import java.util.UUID;

public interface FcmService {
    public void sendToManySilent(List<String> fcmTokens, FcmPostResponseDto response);
    public FcmPostResponseDto createFcmPostResponse(UUID senderId, UUID postId);
}
