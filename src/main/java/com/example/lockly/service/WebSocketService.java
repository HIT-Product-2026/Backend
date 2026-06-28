package com.example.lockly.service;

import java.util.UUID;

public interface WebSocketService {
    public void sendTextMessage(UUID conversationId, Object payload);
    public void sendImageMessage(UUID conversationId, Object payload);
    public void shareLocationToFriend(UUID friendId, Object payload);
}
