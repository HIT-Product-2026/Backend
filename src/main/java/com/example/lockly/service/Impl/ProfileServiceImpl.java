package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.main.PostsRepository;
import com.example.lockly.repository.main.ProfileRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final AuthService authService;
    private final PostsRepository postsRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public void createProfile(UUID userId, CreateProfileRequestDto request){

        User user = authService.getCurrentUser();

        int postCount = postsRepository.countByUserId(userId);

        Profile profile = Profile.builder()
                .user(user)
                .birthday(request.birthday())
                .hobbies(request.hobbies())
                .phoneNumber(request.phoneNumber())
                .postCount(postCount)
                .build();
    }


    // Cập nhật tiến trình nhiệm vụ
    @Override
    @Transactional
    public void updatePostProcessProfile(UUID postId){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        User user = post.getUser();

        Profile profile = profileRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "user id", user.getId()));

        // Kiểm tra streak
        Post oldLatestPost = profile.getLatestPost();
        if (oldLatestPost != null){
            long days = ChronoUnit.DAYS.between(
                    oldLatestPost.getCreatedAt().toLocalDate(),
                    post.getCreatedAt().toLocalDate()
            );
            if (days == 0){
                // Đã điểm danh rồi, không thay đổi streak
            } else if (days == 1){
                // Điểm danh thành công
                profile.setCurrentPostStreak(profile.getCurrentPostStreak() + 1);

                // Cập nhật streak dài nhất
                profile.setLongestPostStreak(Math.max(
                        profile.getLongestPostStreak(),
                        profile.getCurrentPostStreak()
                ));
            } else {
                // Mất streak
                profile.setCurrentPostStreak(0);
            }


        } else {
            profile.setLongestPostStreak(1);
            profile.setCurrentPostStreak(1);
        }

        // Cập nhật bài viết mới trong profile
        profile.setLatestPost(post);

        // Tăng số lượng bài viết đã đăng
        profile.setPostCount(profile.getPostCount() + 1);
    }

    @Override
    public void reviewAchievement(UUID profileId){}

}
