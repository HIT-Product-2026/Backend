package com.example.lockly.service.Impl;

import com.example.lockly.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendTextMessage(
            String conversationId,
            Object payload
    ) {

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                payload
        );
    }

    public void sendImageMessage(
            String conversationId,
            Object payload
    ) {

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                payload
        );
    }


//     Send queue

    public void sendToQueue(
            String queue,
            Object payload
    ) {

        messagingTemplate.convertAndSend(
                "/queue/" + queue,
                payload
        );

    }


//     Send private message
    public void sendToUser(
            String username,
            Object payload
    ) {

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/messages",
                payload
        );

    }
}
