package com.example.lockly.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface SseService {
    SseEmitter subscribeDetectNsfw(UUID taskId) ;
    void push(String userId, String eventType, Object response);
    void disconnect(String userId);
}
