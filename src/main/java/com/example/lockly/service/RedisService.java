package com.example.lockly.service;

import com.example.lockly.domain.dto.response.LocationUserResponseDto;

public interface RedisService {
    public void saveUserLocation(String userId, Double latitude, Double longitude);
    public LocationUserResponseDto getUserLocation(String userId);
}
