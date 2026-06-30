package com.example.lockly.listener;

import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.constant.EventType;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.NsfwStatus;
import com.example.lockly.domain.entity.User;
import com.example.lockly.service.AIService;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.FcmService;
import com.example.lockly.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final FcmService fcmService;
    private final AIService aiService;
    private final SseService sseService;

    // Gửi thông báo fcm
    @RabbitListener(queues = RabbitMQConfig.POST_NOTIFICATION_QUEUE)
    public void sendFcmNotification(FcmNotificationRequestDto message) {
        fcmService.sendToManySilent(message);
    }

    @RabbitListener(queues = RabbitMQConfig.POST_NOTIFICATION_QUEUE)
    public void detectNsfw(DetectNsfwPostRequestDto data) throws IOException {

        boolean isNfws = aiService.detectNsfw(data.file());
        NsfwStatus nsfw;

        if (isNfws) {
            nsfw = NsfwStatus.TRUE;
        }
        else {
            nsfw = NsfwStatus.FALSE;
        }

        PostResponseDto response = PostResponseDto.from(data, nsfw);

        // Trả response qua SSE
        sseService.push(
                data.user().id().toString(),
                EventType.detectNsfw,
                response
                );

        // Đóng kết nối sse sau khi xong
        sseService.disconnect(data.user().id().toString());
    }
}