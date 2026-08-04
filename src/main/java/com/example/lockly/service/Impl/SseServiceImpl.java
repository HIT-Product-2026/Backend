package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor =
            Executors.newSingleThreadScheduledExecutor();

    @Override
    public SseEmitter subscribeDetectNsfw(UUID userId) {
        String id = userId.toString();

        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(id, emitter);

        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> emitters.remove(id));
        emitter.onError(e -> emitters.remove(id));

        log.info("Subscribe thành công");

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("connected")
            );
        } catch (Exception e) {
            emitter.complete();
            emitters.remove(id);
            return emitter;
        }


        return emitter;
    }

    @Override
    public void push(String userId, String eventType, PostResponseDto response) {

        log.info("[SSE] Push result {}", response.id());

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            return;
        }

        log.info("Chuẩn bị gửi response cho SSE");

        try {
            emitter.send(
                    SseEmitter.event()
                            .name(eventType)
                            .data(response)
            );

            log.info("Gửi SSE thành công");

        } catch (Exception e) {
            emitter.complete();
            emitters.remove(userId);
        }
    }

    @Override
    public void disconnect(String userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    @PostConstruct
    private void startHeartbeat() {

        heartbeatExecutor.scheduleAtFixedRate(() -> {
            emitters.forEach((userId, emitter) -> {

                try {
                    emitter.send(
                            SseEmitter.event()
                                    .name("heartbeat")
                                    .data("ping")
                    );
                    log.debug("[SSE] Heartbeat {}", userId);
                } catch (Exception e) {
                    log.warn("[SSE] Heartbeat failed {}", userId);

                    emitter.complete();
                    emitters.remove(userId);
                }

            });

        }, 30, 30, TimeUnit.SECONDS);
    }
}
