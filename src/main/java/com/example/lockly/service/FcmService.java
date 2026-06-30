package com.example.lockly.service;

import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;

import java.util.List;
import java.util.UUID;

public interface FcmService {
    public void sendToManySilent(FcmNotificationRequestDto data);
    public FcmNotificationRequestDto createFcmNotificationRequest(UUID senderId, UUID postId, List<String> fcmTokens);
}
