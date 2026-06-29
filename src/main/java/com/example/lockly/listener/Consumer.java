package com.example.lockly.listener;

import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final FcmService fcmService;

    // Gửi thông báo fcm
    @RabbitListener(queues = RabbitMQConfig.POST_NOTIFICATION_QUEUE)
    public void sendFcmNotification(FcmNotificationRequestDto message) {
        fcmService.sendToManySilent(message);

        // Trả response qua SSE
    }
}