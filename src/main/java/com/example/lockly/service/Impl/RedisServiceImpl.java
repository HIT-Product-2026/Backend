package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.service.RedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisServiceImpl implements RedisService {

    RedisTemplate<String,Object> redisTemplate;
    String userLocationKey = "user:location:";

    @Override
    @Transactional
    public void saveUserLocation(UUID userId, Double latitude, Double longitude){
        String key = userLocationKey + userId;

        redisTemplate.opsForHash().put(key, "latitude", latitude);
        redisTemplate.opsForHash().put(key, "longitude", longitude);
        redisTemplate.opsForHash().put(key, "lastActiveAt", LocalDateTime.now());

        // Xóa sau 5 phút
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
    }

    @Override
    public LocationUserResponseDto getUserLocation(UUID userId){
        String key = userLocationKey + userId;

        Double latitude = (Double) redisTemplate.opsForHash().get(key, "latitude");
        Double longitude = (Double) redisTemplate.opsForHash().get(key, "longitude");
        LocalDateTime lastActiveAt = (LocalDateTime) redisTemplate.opsForHash().get(key, "lastActiveAt");

        return new LocationUserResponseDto(latitude, longitude, lastActiveAt);
    }
}
