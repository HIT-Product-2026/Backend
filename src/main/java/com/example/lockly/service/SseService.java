package com.example.lockly.service;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface SseService {
    SseEmitter subscribeDetectNsfw(UUID taskId) ;
    void push(String userId, String eventType, PostResponseDto response);
    void disconnect(String userId);
}
