package com.example.lockly.service;

import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.entity.User;

import java.util.UUID;

public interface RedisService {
    void saveUserLocation(UUID userId, Double latitude, Double longitude);
    LocationUserResponseDto getUserLocation(UUID userId);
    void saveUser(UserCacheDto user);
    UserCacheDto getUser(UUID userId);
}
