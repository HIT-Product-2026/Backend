package com.example.lockly.service;

import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;

public interface RabbitMQService {
    void sendFcmNotification(FcmNotificationRequestDto data);

}
