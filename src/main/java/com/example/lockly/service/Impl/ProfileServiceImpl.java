package com.example.lockly.service.Impl;

import com.example.lockly.common.util.DateUtil;
import com.example.lockly.common.util.FileUtil;
import com.example.lockly.common.util.LocationUtil;
import com.example.lockly.domain.dto.query.ProvinceInfo;
import com.example.lockly.domain.dto.request.UpdateProfileRequestDto;
import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.repository.location.GISProvinceRepository;
import com.example.lockly.repository.main.*;
import com.example.lockly.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final AuthService authService;
    private final PostsRepository postsRepository;
    private final ProfileRepository profileRepository;
    private final GISProvinceRepository gisProvinceRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    private final FriendshipsRepository friendshipsRepository;
    private final MinIOService minIOService;

    private final String prefixFace = "users/face";

    @Override
    @Transactional
    public void createProfile(User user, CreateProfileRequestDto request){

        int postCount = postsRepository.countByUserId(user.getId());

        LocalDate birthday = DateUtil.parseDdMmYyyy(request.birthday());

        Profile profile = Profile.builder()
                .user(user)
                .birthday(birthday)
                .gender(request.gender())
                .phoneNumber(request.phoneNumber())
                .postCount(postCount)
                .build();

        profileRepository.save(profile);
    }

    @Override
    public void updateProfile(UpdateProfileRequestDto request){
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        Profile profile = profileRepository
                .findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "user id", user.getId()));

        LocalDate birthday = DateUtil.parseDdMmYyyy(request.birthday());

        profile.setBirthday(birthday);
        profile.setGender(request.gender());
        profile.setPhoneNumber(request.phoneNumber());

        profileRepository.save(profile);

        log.info("Update profile object avatar find is: "+  user.getObjectNameAvatar());

    }

    @Override
    @Transactional
    public Boolean registerFace(UUID userId, MultipartFile file) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", userId));

        String objectName = FileUtil.getObjectNameFile(prefixFace, userId);

        // Lưu ảnh trước rồi mới check
        minIOService.saveFile(file, objectName);

        return aiService.registerFace(userId, objectName);
    }

    @Override
    public Boolean checkFace(UUID userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", userId));

        return aiService.checkFace(userId);
    }

    @Override
    @Transactional
    public void updateProcessProfile(UUID postId, String objectName){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        User user = post.getUser();

        // Lấy Profile
        Optional<Profile> optionalProfile = profileRepository.findByUserId(user.getId());

        // Dừng cập nhật nếu chưa có profile
        if (optionalProfile.isEmpty()) {
            return;
        }
        Profile profile = optionalProfile.get();

        Double latitude = post.getLatitude();
        Double longitude = post.getLongitude();

        // Cập nhật tiến trình streak, latest post, số lượng post
        post = updatePostProfile(post);

        // Cập nhật tiến trình "Nhà thám hiểm"
        profile = updateCityVisited(profile, latitude, longitude);

        // Cập nhật tiến trình "Kế thừa di sản"
        profile = updatePostReupped(profile, latitude, longitude);

        // Cập nhật tiến trình "Lời hứa năm xưa"
        profile = updateProcessPhotoWithFriends(profile, objectName);

        profileRepository.save(profile);
        postsRepository.save(post);
    }

    // Cập nhật tiến trình liên quan đến post
    @Override
    @Transactional
    public Post updatePostProfile(Post post){

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

        return post;
    }

    // Cập số thành phố đã đi
    @Override
    public Profile updateCityVisited(Profile profile, Double latitude, Double longitude){

        ProvinceInfo provinceInfo = gisProvinceRepository
                .findProvinceByLocation(latitude, longitude)
                .orElse(null);

        // Nếu địa điểm nằm ngoài vũ trụ thì chịu, không cập nhật được
        if (provinceInfo == null){
            return profile;
        }

        String cityCode = provinceInfo.getCode();

        // Thêm 1 thành phố đã đi (có set nên không sợ trùng)
        if (cityCode != null){
            profile.getCityCodeList().add(cityCode);
        }

        return profile;
    }

    // Cập nhật số bài viết được reup
    @Override
    public Profile updatePostReupped(
            Profile profile,
            Double latitude,
            Double longitude
    ){

        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        List<Post> completedPosts =
                postsRepository.findFriendPostsWithinDistance(
                        user.getId(),
                        FriendshipStatus.ACCEPTED.name(),
                        longitude,
                        latitude
                );


        if(completedPosts.isEmpty()){
            return profile;
        }


        boolean canUnlock =
                postsRepository.existsUnlockablePost(
                        profile.getId(),
                        completedPosts.stream()
                                .map(Post::getId)
                                .toList()
                );


        if(canUnlock){

            profile.setCountPostReup(
                    profile.getCountPostReup()+1
            );


            profile.getReupPostIds()
                    .addAll(
                            completedPosts.stream()
                                    .map(Post::getId)
                                    .toList()
                    );
        }


        return profile;
    }

    // Cập nhật các bạn bè đã được chụp chung
    @Override
    @Transactional
    public Profile updateProcessPhotoWithFriends(Profile profile, String objectName) {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        List<UUID> detectedIds;
        try {
            detectedIds = aiService.detectFaces(objectName);
        } catch (Exception e) {
            throw new RuntimeException("AI Service timeout", e);
        }

        List<UUID> friendIds = friendshipsRepository.findFriendIds(
                user.getId(),
                FriendshipStatus.ACCEPTED
        );

        Set<UUID> friendIdSet = new HashSet<>(friendIds);

        Set<UUID> friendsInPhoto = detectedIds.stream()
                .filter(friendIdSet::contains)
                .collect(Collectors.toSet());

        if (friendsInPhoto.isEmpty()) {
            return profile;
        }

        profile.getPhotographedFriendIds().addAll(friendsInPhoto);

        return profile;
    }
}
