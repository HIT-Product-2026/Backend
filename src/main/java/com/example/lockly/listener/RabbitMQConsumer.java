package com.example.lockly.listener;

import com.example.lockly.config.MinioProperties;
import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.constant.EventType;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.enumEntity.NsfwStatus;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.main.PostsRepository;
import com.example.lockly.service.AIService;
import com.example.lockly.service.FcmService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final FcmService fcmService;
    private final AIService aiService;
    private final SseService sseService;
    private final PostsRepository postsRepository;

    // Gửi thông báo fcm
    @RabbitListener(
            queues = RabbitMQConfig.POST_NOTIFICATION_QUEUE,
            concurrency = "1-5"
    )
    public void sendFcmNotification(FcmNotificationRequestDto message) {
        fcmService.sendToManySilent(message);
    }

    @RabbitListener(
            queues = RabbitMQConfig.IMAGE_NSFW_QUEUE,
            concurrency = "1-3"
    )
    public void detectNsfw(DetectNsfwPostRequestDto data) {

        log.info("Received message id={}", data.postId());

        // Giá trị mặc định, tránh lỗi
        boolean isNfws = aiService.detectNsfw(data.objectName());

        NsfwStatus nsfw;

        if (isNfws) {
            nsfw = NsfwStatus.TRUE;
        }
        else {
            nsfw = NsfwStatus.FALSE;
        }

        Post post = postsRepository
                .findById(data.postId())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", data.postId()));

        post.setNsfw(nsfw);
        postsRepository.save(post);

        PostResponseDto response = PostResponseDto.from(data, nsfw);

        // Trả response qua SSE
        sseService.push(
                data.user().id().toString(),
                EventType.detectNsfw,
                response
                );

        ScheduledExecutorService executor =
                Executors.newSingleThreadScheduledExecutor();

        // delay an toàn để đảm bảo flush network
        executor.schedule(
                // Đóng connect sse
                () -> sseService.disconnect(data.user().id().toString()),
                2,
                TimeUnit.SECONDS
        );
    }
}