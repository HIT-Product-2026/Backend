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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class Consumer {

    private final FcmService fcmService;
    private final AIService aiService;
    private final SseService sseService;
    private final TaskScheduler scheduler;

    // Gửi thông báo fcm
    @RabbitListener(queues = RabbitMQConfig.POST_NOTIFICATION_QUEUE)
    public void sendFcmNotification(FcmNotificationRequestDto message) {
        fcmService.sendToManySilent(message);
    }

    @RabbitListener(queues = RabbitMQConfig.IMAGE_NSFW_QUEUE)
    public void detectNsfw(DetectNsfwPostRequestDto data) throws IOException {

        // Giá trị mặc định, tránh lỗi
        boolean isNfws = false;

        try {
            isNfws = aiService.detectNsfw(data.file());
        }catch (Exception e){
            log.error("Error detecting NSFW image. userId={}, postId={}",
                    data.user().id(),
                    data.postId(),
                    e);
        }

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

        // delay an toàn để đảm bảo flush network
        scheduler.schedule(
                // Đóng kết nối sse sau khi xong
                () -> sseService.disconnect(data.user().id().toString()),
                new Date(System.currentTimeMillis() + 2000)
        );
    }
}