package com.example.lockly.service.Impl;

import com.example.lockly.config.RabbitMQConfig;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.service.AIService;
import com.example.lockly.service.RabbitMQService;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
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

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.IMAGE_DETECT_KEY,
                data
        );
    }
}
