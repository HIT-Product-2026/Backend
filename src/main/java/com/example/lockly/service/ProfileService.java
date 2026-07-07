package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProfileService {
    void createProfile(UUID userId, CreateProfileRequestDto request);
    void registerFace(UUID userId, MultipartFile image) ;

    void updateProcessProfile(UUID postId, MultipartFile image);

    void updatePostProfile(UUID postId);
    void updateCityVisited(UUID profileId, Double latitude, Double longitude);
    void updatePostReupped(UUID profileId, Double latitude, Double longitude);
    void updateProcessPhotoWithFriends(UUID profileId, MultipartFile image);
}
