package com.example.lockly.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface SseService {
    public SseEmitter subscribeDetectNsfw(UUID taskId) ;
    public void push(String userId, Object message) ;
}
