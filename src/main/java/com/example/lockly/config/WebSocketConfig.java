package com.example.lockly.config;

import com.example.lockly.security.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Qualifier("websocketHeartbeatScheduler")
    private final TaskScheduler websocketHeartbeatScheduler;

    public WebSocketConfig(
            JwtChannelInterceptor jwtChannelInterceptor,
            @Qualifier("websocketHeartbeatScheduler")
            TaskScheduler websocketHeartbeatScheduler
    ) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
        this.websocketHeartbeatScheduler = websocketHeartbeatScheduler;
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        log.debug("Connect WebSocket");
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setApplicationDestinationPrefixes("/app");

        registry.enableSimpleBroker(
                        "/topic",
                        "/queue"
                )
                .setTaskScheduler(websocketHeartbeatScheduler)
                .setHeartbeatValue(new long[]{10000, 10000});

        registry.setUserDestinationPrefix("/user");
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}