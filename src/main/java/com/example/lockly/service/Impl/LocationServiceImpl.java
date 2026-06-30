package com.example.lockly.service.Impl;

import com.example.lockly.common.util.LocationUtil;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.entity.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.LocationService;
import com.example.lockly.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final RedisService redisService;
    private final AuthService authService;

    private final double distanceLimit = 5;
    private final long timeLimit = 10;

    @Override
    public boolean isDropRequest(Double longitude, Double latitude){
        User user = authService.getCurrentUser();

        LocationUserResponseDto dto = redisService.getUserLocation(user.getId());

        if (longitude == null || latitude == null)
            return true; // Drop request do client cung cấp thiếu tọa độ

        if (dto.longitude() == null || dto.latitude() == null)
            return false; // Không drop request do chưa có tọa độ nào được lưu trữ

        long duration = Duration
                .between(LocalDateTime.now(), dto.lastActiveAt())
                .toSeconds();

        // Tần suất request trên timeLimit thì chấp nhận (tính bằng giây)
        if (duration > timeLimit)
            return false;

        // Chuyển sang double
        double longitude1 = dto.longitude();
        double latitude1 = dto.latitude();
        double longitude2 = longitude;
        double latitude2 = latitude;

        // Khoảng cách (tính bằng mét)
        double distance = LocationUtil.calculateDistance(
                latitude1, longitude1,
                latitude2, longitude2
                );

        // Di chuyển ít hơn distanceLimit thì drop
        if (distance < distanceLimit)
            return true;

        return false;
    }
}
