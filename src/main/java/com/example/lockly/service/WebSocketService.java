package com.example.lockly.service;

import com.example.lockly.domain.entity.main.User;

import java.util.UUID;

public interface WebSocketService {
    void sendTextMessage(UUID conversationId, Object payload);
    void sendImageMessage(UUID conversationId, Object payload);
    void shareLocationToFriend(User user, Object payload);
    void shareOnlineToFriend(UUID userId, Object payload);

    void pubMessageToConversations(String user, Object payload);
}
