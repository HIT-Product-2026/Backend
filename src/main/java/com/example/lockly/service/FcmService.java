package com.example.lockly.service;

import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;

import java.util.List;
import java.util.UUID;

public interface FcmService {
    void sendToManySilent(FcmNotificationRequestDto data);
}
