package com.example.lockly.service;

import com.example.lockly.domain.dto.response.LocationUserResponseDto;

import java.util.UUID;

public interface RedisService {
    void saveUserLocation(UUID userId, Double latitude, Double longitude);
    LocationUserResponseDto getUserLocation(UUID userId);
}
