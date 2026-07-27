package com.example.lockly.service;

import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.UUID;

public interface NotificationService {
    String sendMessageNotification(
            String token,
            MessageResponseDto notification,
            UUID conversationId
    )  throws FirebaseMessagingException;
}
