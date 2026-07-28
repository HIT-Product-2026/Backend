package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.request.auth.ForgotPasswordCacheDto;
import com.example.lockly.domain.dto.request.auth.RegisterCacheDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String,Object> redisTemplate;

    private final String userLocationKey = "user:location:";
    private final String userOnlineKey = "user.online";
    private final String REGISTER_KEY = "register:";
    private final Duration REGISTER_TTL = Duration.ofMinutes(5);
    private static final String FORGOT_PASSWORD_KEY = "forgot:";
    private static final Duration FORGOT_PASSWORD_TTL = Duration.ofMinutes(5);
    private static final String FORGOT_PASSWORD_VERIFIED_KEY = "forgot:verified:";
    private static final Duration FORGOT_PASSWORD_VERIFIED_TTL = Duration.ofMinutes(10);
    private static final String ACCESS_TOKEN_KEY = "user:access:";
    private static final String REFRESH_TOKEN_KEY = "user:refresh:";


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

        return new LocationUserResponseDto(userId, latitude, longitude, lastActiveAt);
    }

    @Override
    public void saveUser(UserCacheDto user) {
        redisTemplate.opsForValue().set(
                "user:" + user.id(),
                user,
                Duration.ofHours(1)
        );
    }

    @Override
    public UserCacheDto getUser(UUID userId) {
        return (UserCacheDto) redisTemplate.opsForValue().get("user:" + userId);
    }

    @Override
    public void saveUserOnline(UUID userId, boolean isOnline){
        String key = userOnlineKey;

        redisTemplate.opsForHash().put(key, "isOnline", isOnline);
    }

    @Override
    public Boolean getUserOnline(UUID userId){
        String key = userOnlineKey + userId;

        Boolean isOnline = (Boolean) redisTemplate.opsForHash().get(key, "isOnline");

        return isOnline;
    }

    @Override
    public void saveRegister(String email, RegisterCacheDto register) {

        redisTemplate.opsForValue().set(
                REGISTER_KEY + email,
                register,
                REGISTER_TTL
        );
    }

    @Override
    public RegisterCacheDto getRegister(String email) {

        return (RegisterCacheDto) redisTemplate
                .opsForValue()
                .get(REGISTER_KEY + email);
    }

    @Override
    public void deleteRegister(String email) {

        redisTemplate.delete(REGISTER_KEY + email);
    }

    @Override
    public void saveForgotPassword(String email, ForgotPasswordCacheDto forgotPassword) {

        redisTemplate.opsForValue().set(
                FORGOT_PASSWORD_KEY + email,
                forgotPassword,
                FORGOT_PASSWORD_TTL
        );
    }

    @Override
    public ForgotPasswordCacheDto getForgotPassword(String email) {

        return (ForgotPasswordCacheDto) redisTemplate
                .opsForValue()
                .get(FORGOT_PASSWORD_KEY + email);
    }

    @Override
    public void deleteForgotPassword(String email) {

        redisTemplate.delete(FORGOT_PASSWORD_KEY + email);
    }

    @Override
    public void saveForgotPasswordVerified(String email) {

        redisTemplate.opsForValue().set(
                FORGOT_PASSWORD_VERIFIED_KEY + email,
                Boolean.TRUE,
                FORGOT_PASSWORD_VERIFIED_TTL
        );
    }

    @Override
    public Boolean isForgotPasswordVerified(String email) {

        return (Boolean) redisTemplate.opsForValue()
                .get(FORGOT_PASSWORD_VERIFIED_KEY + email);
    }

    @Override
    public void deleteForgotPasswordVerified(String email) {

        redisTemplate.delete(FORGOT_PASSWORD_VERIFIED_KEY + email);
    }

    @Override
    public void saveAccessToken(UUID userId, String accessJti, long expirationMillis) {

        redisTemplate.opsForValue().set(
                ACCESS_TOKEN_KEY + userId,
                accessJti,
                Duration.ofMillis(expirationMillis)
        );
    }

    @Override
    public String getAccessToken(UUID userId) {

        return (String) redisTemplate.opsForValue()
                .get(ACCESS_TOKEN_KEY + userId);
    }

    @Override
    public void deleteAccessToken(UUID userId) {

        redisTemplate.delete(ACCESS_TOKEN_KEY + userId);
    }

    @Override
    public void saveRefreshToken(UUID userId, String refreshJti, long expirationMillis) {

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY + userId,
                refreshJti,
                Duration.ofMillis(expirationMillis)
        );
    }

    @Override
    public String getRefreshToken(UUID userId) {

        return (String) redisTemplate.opsForValue()
                .get(REFRESH_TOKEN_KEY + userId);
    }

    @Override
    public void deleteRefreshToken(UUID userId) {

        redisTemplate.delete(REFRESH_TOKEN_KEY + userId);
    }
}
