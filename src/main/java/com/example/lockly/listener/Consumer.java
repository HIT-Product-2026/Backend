package com.example.lockly.listener;

import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.NsfwStatus;
import com.example.lockly.service.AIService;
import com.example.lockly.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class Consumer {

    private final FcmService fcmService;
    private final AIService aiService;

    // Gửi thông báo fcm
    @RabbitListener(queues = RabbitMQConfig.POST_NOTIFICATION_QUEUE)
    public void sendFcmNotification(FcmNotificationRequestDto message) {
        fcmService.sendToManySilent(message);

        // Trả response qua SSE
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
    }
}