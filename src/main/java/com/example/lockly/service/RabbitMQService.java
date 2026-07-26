package com.example.lockly.service;

import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface RabbitMQService {
    void sendFcmNotification(UUID senderId, UUID responseId);
    void detectNsfw(DetectNsfwPostRequestDto message);

}
