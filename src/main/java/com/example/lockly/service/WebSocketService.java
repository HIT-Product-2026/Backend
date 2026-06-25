package com.example.lockly.service;

public interface WebSocketService {
    public void sendTextMessage(String conversationId, Object payload);
    public void sendImageMessage(String conversationId, Object payload);
    public void shareLocationToFriend(String friendId, Object payload);
}
