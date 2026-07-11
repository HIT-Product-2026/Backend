package com.example.lockly.service;

import java.util.UUID;

public interface WebSocketService {
    void sendTextMessage(UUID conversationId, Object payload);
    void sendImageMessage(UUID conversationId, Object payload);
    void shareLocationToFriend(UUID userId, Object payload);
    void shareOnlineToFriend(UUID userId, Object payload);
}
