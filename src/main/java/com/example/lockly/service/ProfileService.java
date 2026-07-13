package com.example.lockly.service;

import com.example.lockly.domain.dto.request.UpdateProfileRequestDto;
import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;

import java.util.UUID;

public interface ProfileService {
    void createProfile(User user, CreateProfileRequestDto request);
    void registerFace(UUID userId, String objectName) ;

    void updateProcessProfile(UUID postId, String objectName);

    Post updatePostProfile(Post post);
    Profile updateCityVisited(Profile profile, Double latitude, Double longitude);
    Profile updatePostReupped(Profile profile, Double latitude, Double longitude);
    Profile updateProcessPhotoWithFriends(Profile profile, String objectName);
    void updateProfile(UpdateProfileRequestDto request);
}
