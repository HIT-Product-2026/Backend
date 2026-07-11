package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.Profile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProfileService {
    void createProfile(UUID userId, CreateProfileRequestDto request);
    void registerFace(UUID userId, MultipartFile image) ;

    void updateProcessProfile(UUID postId, MultipartFile image);

    Post updatePostProfile(Post post);
    Profile updateCityVisited(Profile profile, Double latitude, Double longitude);
    Profile updatePostReupped(Profile profile, Double latitude, Double longitude);
    Profile updateProcessPhotoWithFriends(Profile profile, MultipartFile image);
}
