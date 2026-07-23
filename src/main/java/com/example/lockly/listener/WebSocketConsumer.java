package com.example.lockly.listener;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketConsumer {

    private final RedisService redisService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        Authentication authentication = (Authentication) event.getUser();

        if (authentication == null) {
            return;
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        redisService.saveUserOnline(user.getId(), false);
    }
}
