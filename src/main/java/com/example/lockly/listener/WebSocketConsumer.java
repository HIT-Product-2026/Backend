package com.example.lockly.listener;

import com.example.lockly.service.RedisService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Component
public class WebSocketConsumer {

    RedisService redisService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        UUID userId = UUID.fromString(principal.getName());

        if (principal != null) {
            // Đánh dấu offline
            redisService.saveUserOnline(userId, false);
        }
    }
}
