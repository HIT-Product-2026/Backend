package com.example.lockly.service.Impl;

import com.example.lockly.common.util.LocationUtil;
import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.location.GISProvinceRepository;
import com.example.lockly.repository.main.PostsRepository;
import com.example.lockly.repository.main.ProfileRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ProfileService;
import com.example.lockly.service.UserService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final AuthService authService;
    private final PostsRepository postsRepository;
    private final ProfileRepository profileRepository;
    private final GISProvinceRepository gisProvinceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void
    createProfile(UUID userId, CreateProfileRequestDto request){

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

    @Override
    public void updateProcessProfile(UUID postId){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        User user = post.getUser();

        Profile profile = profileRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "user id", user.getId()));

        Double latitude = post.getLatitude();
        Double longitude = post.getLongitude();

        // Cập nhật tiến trình streak, latest post, số lượng post
        updatePostProfile(postId);

        // Cập nhật tiến trình "Nhà thám hiểm"
        updateCityVisited(profile.getId(), latitude, longitude);

        // Cập nhật tiến trình "Kế thừa di sản"
        updatePostReupped(profile.getId(), latitude, longitude);

    }

    // Cập nhật tiến trình liên quan đến post
    @Override
    @Transactional
    public void updatePostProfile(UUID postId){
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

    // Cập số thành phố đã đi
    @Transactional
    @Override
    public void updateCityVisited(UUID profileId, Double latitude, Double longitude){
        Profile profile = profileRepository
                .findByUserId(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));

        String cityCode = gisProvinceRepository
                .findProvinceCodeByLocation(latitude, longitude)
                .orElse(null);

        // Thêm 1 thành phố đã đi (có set nên không sợ trùng)
        if (cityCode != null){
            profile.getCityCodeList().add(cityCode);
        }
    }

    // Cập nhật số bài viết được reup
    @Transactional
    @Override
    public void updatePostReupped(UUID profileId, Double latitude, Double longitude){
        Profile profile = profileRepository
                .findByUserId(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));

        User user = authService.getCurrentUser();

        // Danh sách bài post thỏa mãn điều kiện
        List<Post> postCompleteds = new ArrayList<>();

        // Lấy toàn bộ bài post mà friends có
        final int pageSize = 100;
        int pageNumber = 0;
        Slice<Post> slice;

        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            slice = postsRepository.findFriendPosts(
                    user.getId(),
                    FriendshipStatus.ACCEPTED,
                    pageable
            );

            for (Post post : slice.getContent()) {
                // Xử lý từng post
                // Kiểm tra bài post nào thỏa mã điều kiện tiến trình
                double distance = LocationUtil.calculateDistance(
                        latitude, longitude,
                        post.getLatitude(), post.getLongitude()
                );

                // Khoảng cách dưới 100m là thỏa mãn
                if (distance <= 100){
                    postCompleteds.add(post);
                }
            }

            pageNumber++;
        } while (slice.hasNext());


        // Kiểm tra xem bài post đã được lưu trong profile chưa
        Set<UUID> reupPostIds = profile.getReupPostIds();

        List<Post> markedPosts = reupPostIds.isEmpty()
                ? new ArrayList<>()
                : postsRepository.findAllById(reupPostIds);

        boolean canUnlock = false;

        // Chỉ cần tìm được 1 candidate hợp lệ
        for (Post candidate : postCompleteds) {

            if (reupPostIds.contains(candidate.getId())) {
                continue;
            }

            boolean farEnough = true;

            for (Post marked : markedPosts) {
                double distance = LocationUtil.calculateDistance(
                        candidate.getLatitude(), candidate.getLongitude(),
                        marked.getLatitude(), marked.getLongitude()
                );

                if (distance < 1000) {
                    farEnough = false;
                    break;
                }
            }

            if (farEnough) {
                canUnlock = true;
                break;
            }
        }

        // Nếu kích hoạt thành công thì lưu toàn bộ postCompleted
        if (canUnlock) {
            profile.setCountPostReup(profile.getCountPostReup() + 1);

            reupPostIds.addAll(
                    postCompleteds.stream()
                            .map(Post::getId)
                            .toList()
            );
        }
    }
}
