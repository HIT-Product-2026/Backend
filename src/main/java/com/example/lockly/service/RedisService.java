package com.example.lockly.service;

import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;

import java.util.UUID;

public interface RedisService {
    void saveUserLocation(UUID userId, Double latitude, Double longitude);
    void saveUser(UserCacheDto user);
    void saveUserOnline(UUID userId, boolean isOnline);

    LocationUserResponseDto getUserLocation(UUID userId);
    UserCacheDto getUser(UUID userId);
    Boolean getUserOnline(UUID userId);
}
