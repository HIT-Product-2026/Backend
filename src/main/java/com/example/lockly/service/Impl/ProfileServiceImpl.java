package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.Profile;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.PostsRepository;
import com.example.lockly.repository.ProfileRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final AuthService authService;
    private final PostsRepository postsRepository;
    private final ProfileRepository profileRepository;

    @Override
    public void createProfile(UUID userId, CreateProfileRequestDto request){

        User user = authService.getCurrentUser();

        int postCount = postsRepository.findByUserIdInOrderByCreatedAtDesc(userId).size();

        Profile profile = Profile.builder()
                .user(user)
                .birthday(request.birthday())
                .hobbies(request.hobbies())
                .phoneNumber(request.phoneNumber())
                .postCount(postCount)
                .build();

        profileRepository.save(profile);
    }


    // Cập nhật tiến trình nhiệm vụ
    @Override
    public void updateProcessProfile(UUID postId){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Nhà thám hiểm
    }

    @Override
    public void reviewAchievement(UUID profileId){}
}
