package com.example.lockly.service.Impl;

import com.example.lockly.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendTextMessage(
            UUID conversationId,
            Object payload
    ) {
        log.debug("Gửi message cho conversation id: " + conversationId);

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                payload
        );
    }

    @Override
    public void sendImageMessage(
            UUID conversationId,
            Object payload
    ) {

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                payload
        );
    }

    @Override
    public void pubMessageToConversations(
            String user,
            Object payload
    ){


        messagingTemplate.convertAndSendToUser(
                user,
                "/queue/conversation",
                payload
        );
    }

//     Send private message
    @Override
    public void shareLocationToFriend(
            UUID userId,
            Object payload
    ) {

        messagingTemplate.convertAndSend(
                "/topic/location/" + userId,
                payload
        );
    }

    // Chia sẻ trạng thái online của người dùng
    @Override
    public void shareOnlineToFriend(
            UUID userId,
            Object payload
    ){
        messagingTemplate.convertAndSend(
                "/topic/online/" + userId,
                payload
        );
    }
}
