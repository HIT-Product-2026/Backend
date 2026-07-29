package com.example.lockly.service;

import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.request.auth.ForgotPasswordCacheDto;
import com.example.lockly.domain.dto.request.auth.RegisterCacheDto;
import com.example.lockly.domain.dto.response.LocationUserResponseDto;
import com.example.lockly.domain.entity.main.User;

import java.util.UUID;

public interface RedisService {
    void saveUserLocation(UUID userId, Double latitude, Double longitude);
    void saveUser(UserCacheDto user);
    void saveUserOnline(UUID userId, boolean isOnline);
    void saveRegister(String email, RegisterCacheDto register);
    void saveForgotPassword(String email, ForgotPasswordCacheDto forgotPassword);
    void saveForgotPasswordVerified(String email);
    void saveAccessToken(UUID userId, String accessJti, long expirationMillis);
    void saveRefreshToken(UUID userId, String refreshJti, long expirationMillis);

    LocationUserResponseDto getUserLocation(UUID userId);
    UserCacheDto getUser(UUID userId);
    Boolean getUserOnline(UUID userId);
    RegisterCacheDto getRegister(String email);
    ForgotPasswordCacheDto getForgotPassword(String email);
    String getAccessToken(UUID userId);
    String getRefreshToken(UUID userId);

    void deleteRegister(String email);
    void deleteForgotPassword(String email);
    void deleteForgotPasswordVerified(String email);
    void deleteAccessToken(UUID userId);
    void deleteRefreshToken(UUID userId);

    Boolean isForgotPasswordVerified(String email);
}
