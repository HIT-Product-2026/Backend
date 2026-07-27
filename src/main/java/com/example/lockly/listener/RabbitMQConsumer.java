package com.example.lockly.listener;

import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.constant.EventType;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.enumEntity.NsfwStatus;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.exception.nonRetryException.NonRetryableAppException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.exception.retryException.RetryableAppException;
import com.example.lockly.repository.main.PostsRepository;
import com.example.lockly.service.AIService;
import com.example.lockly.service.FcmService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final MinIOService minIOService;

    // Gửi thông báo fcm
    @RabbitListener(
            queues = RabbitMQConfig.POST_NOTIFICATION_QUEUE,
            concurrency = "1-5"
    )
    public void sendFcmNotification(FcmNotificationRequestDto message) {

        try {

            fcmService.sendToManySilent(message);

            log.info("[RabbitMQ][DONE] Send FCM");

        }
        catch (NonRetryableAppException e) {

            log.error("[RabbitMQ][DROP] {}", e.getMessage(), e);

            throw new AmqpRejectAndDontRequeueException(e);
        }
        catch (RetryableAppException e) {

            log.error("[RabbitMQ][RETRY] {}", e.getMessage(), e);

            throw e;
        }
        catch (Exception e) {

            log.error("[RabbitMQ][UNKNOWN]", e);

            throw e;
        }
    }

    @RabbitListener(
            queues = RabbitMQConfig.IMAGE_NSFW_QUEUE,
            concurrency = "1-3"
    )
    @Transactional
    public void detectNsfw(DetectNsfwPostRequestDto data) {
        try {
            log.info("Detect NSFW message: {}", data);
            log.info("Post id: {}", data.postId());
            log.info("Object name: {}", data.objectName());

            boolean isNsfw = aiService.detectNsfw(data.objectName());

            NsfwStatus nsfw = isNsfw
                    ? NsfwStatus.TRUE
                    : NsfwStatus.FALSE;

            Post post = postsRepository
                    .findById(data.postId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Post",
                            "id",
                            data.postId()
                    ));

            post.setNsfw(nsfw);
            postsRepository.save(post);

            String urlImage = minIOService.generatePresignedUrl(post.getObjectName());

            PostResponseDto response = PostResponseDto.from(data, nsfw, urlImage);

            sseService.push(
                    data.user().id().toString(),
                    EventType.detectNsfw,
                    response
            );

            ScheduledExecutorService executor =
                    Executors.newSingleThreadScheduledExecutor();

            executor.schedule(
                    () -> sseService.disconnect(data.user().id().toString()),
                    2,
                    TimeUnit.SECONDS
            );

            log.info(
                    "[RabbitMQ][DONE] postId={}, nsfw={}",
                    data.postId(),
                    nsfw
            );

        }
        // Không retry
        catch (NonRetryableAppException e) {

            log.error(
                    "[RabbitMQ][DROP] postId={}, message={}",
                    data.postId(),
                    e.getMessage(),
                    e
            );

            throw new AmqpRejectAndDontRequeueException(e);
        }
        // Retry
        catch (RetryableAppException e) {

            log.error(
                    "[RabbitMQ][RETRY] postId={}, message={}",
                    data.postId(),
                    e.getMessage(),
                    e
            );

            throw e;
        }
        // Các lỗi chưa xác định -> mặc định retry
        catch (Exception e) {

            log.error(
                    "[RabbitMQ][UNKNOWN] postId={}",
                    data.postId(),
                    e
            );

            throw e;
        }
    }
}