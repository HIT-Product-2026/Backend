package com.example.lockly.service;

import com.example.lockly.domain.dto.response.FcmPostResponseDto;

import java.util.List;

public interface FcmService {
    public void sendToManySilent(List<String> fcmTokens, FcmPostResponseDto response);
    public FcmPostResponseDto createFcmPostResponse(String senderId, String postId);
}
