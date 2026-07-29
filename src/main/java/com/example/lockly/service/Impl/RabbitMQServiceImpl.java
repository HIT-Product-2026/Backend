package com.example.lockly.service.Impl;

import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.RabbitMQService;

import com.example.lockly.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQServiceImpl implements RabbitMQService {

    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;

    @Override
    public void sendFcmNotification(User sender, UUID responseId) {

        // Lấy danh sách fcm của bạn bè
        List<String> fcmTokens = userService.findFcmTokenOfFriendsByUserId(sender);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.POST_KEY,
                FcmNotificationRequestDto.from(
                        sender.getId(),
                        responseId,
                        fcmTokens
                )
        );
    }

    @Override
    public void detectNsfw(DetectNsfwPostRequestDto data){

        log.info(
                "[RabbitMQ][SEND] postId={}, objectName={}",
                data.postId(),
                data.objectName()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.IMAGE_DETECT_KEY,
                data
        );

        log.info(
                "[RabbitMQ][SENT] postId={}",
                data.postId()
        );
    }
}
