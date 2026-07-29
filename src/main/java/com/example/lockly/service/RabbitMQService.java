package com.example.lockly.service;

import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.entity.main.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface RabbitMQService {
    void sendFcmNotification(User sender, UUID responseId);
    void detectNsfw(DetectNsfwPostRequestDto message);

}
