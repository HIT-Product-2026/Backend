package com.example.lockly.service.Impl;

import com.example.lockly.domain.entity.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AuthService authService;

    @Override
    public SseEmitter subscribeDetectNsfw(UUID taskId) {
        String id = taskId.toString();

        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(id, emitter);

        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> emitters.remove(id));
        emitter.onError(e -> emitters.remove(id));

        return emitter;
    }

    @Override
    public void push(String userId, Object message) {

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            return;
        }

        try {
            emitter.send(message);
        } catch (Exception e) {
            emitter.complete();
            emitters.remove(userId);
        }
    }
}
