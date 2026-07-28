package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.exception.retryException.FcmNotificationException;
import com.example.lockly.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public String sendMessageNotification(
            String token,
            MessageResponseDto notification,
            UUID conversationId
    ) {

        try {

            String title = notification.sender().displayName();
            String body = title + ": " + notification.content();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                    .putData("conversationId", conversationId.toString())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("FCM messageId: {}", messageId);
            return messageId;

        } catch (FirebaseMessagingException e) {
            throw new FcmNotificationException(
                    "Lỗi gửi thông báo FCM",
                    e
            );
        }
    }
}
