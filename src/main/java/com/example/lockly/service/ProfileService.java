package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;

import java.util.UUID;

public interface ProfileService {
    void createProfile(UUID userId, CreateProfileRequestDto request);

    void updateProcessProfile(UUID postId);

    void updatePostProfile(UUID postId);
    void updateCityVisited(UUID profileId, Double latitude, Double longitude);
    void updatePostReupped(UUID profileId, Double latitude, Double longitude);
}
