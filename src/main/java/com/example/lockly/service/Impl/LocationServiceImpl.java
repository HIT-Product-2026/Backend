package com.example.lockly.service.Impl;

import com.example.lockly.common.util.LocationUtil;
import com.example.lockly.domain.dto.query.ProvinceInfo;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.repository.location.GISProvinceRepository;
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
    private final GISProvinceRepository gisProvinceRepository;

    @Override
    public boolean isDropRequest(Double longitude, Double latitude){
        final double distanceLimit = 5;
        final long timeLimit = 10;

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

    public boolean isUnknownLocation(Double longitude, Double latitude){
        double safeDistance = 500; // Khoảng cách an toàn là 500m/s (đây là vận tốc của máy bay dân dụng)

        User user = authService.getCurrentUser();

        LocationUserResponseDto dto = redisService.getUserLocation(user.getId());

        if (longitude == null || latitude == null)
            return false; // Không nghi ngờ nếu không có tọa độ

        if (dto.longitude() == null || dto.latitude() == null)
            return false; // Không nghi ngờ nếu chưa có tọa độ

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

        // Thời gian giữa 2 lần cập nhật
        long duration = Duration
                .between(dto.lastActiveAt(), LocalDateTime.now())
                .toSeconds();

        // Nếu khoảng cách giữa 2 lần cập nhật vị trí khả nghi
        if (duration * safeDistance < distance)
            return true;

        return false;
    }

    @Override
    public String getProvinceFullName(Double latitude, Double longitude) {

        return gisProvinceRepository.findProvinceByLocation(latitude, longitude)
                .map(ProvinceInfo::getFullName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Province",
                        "location",
                        latitude + "," + longitude
                ));
    }
}
