package com.example.lockly.service.Impl;

import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.service.RabbitMQService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQServiceImpl implements RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendFcmNotification(FcmNotificationRequestDto data) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.POST_KEY,
                data
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
