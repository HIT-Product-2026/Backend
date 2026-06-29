package com.example.lockly.service;

import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface RabbitMQService {
    void sendFcmNotification(FcmNotificationRequestDto data);
    void detectNsfw(DetectNsfwPostRequestDto message);

}
