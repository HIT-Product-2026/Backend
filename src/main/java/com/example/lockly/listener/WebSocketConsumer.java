package com.example.lockly.listener;

import com.example.lockly.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketConsumer {

    private final RedisService redisService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        Principal principal = event.getUser();

        if (principal == null) {
            return;
        }

        UUID userId = UUID.fromString(principal.getName());

        redisService.saveUserOnline(userId, false);
    }
}
