package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String,Object> redisTemplate;
    private final String userLocationKey = "user:location:";

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

    @Override
    public void saveUser(UserCacheDto user) {
        redisTemplate.opsForValue().set(
                "user:" + user.id(),
                user,
                Duration.ofHours(1)
        );
    }

    public UserCacheDto getUser(UUID userId) {
        return (UserCacheDto) redisTemplate.opsForValue().get("user:" + userId);
    }
}
