package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;

import java.util.UUID;

public interface ProfileService {
    void createProfile(UUID userId, CreateProfileRequestDto request);
    void reviewAchievement(UUID profileId);
    void updatePostProcessProfile(UUID postId);
}
