package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.domain.entity.main.Friendship;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.main.FriendshipsRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.*;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FriendshipsRepository friendshipsRepository;
    private final MinIOService minIOService;
    private final MinioClient minioClient;
    private final MinioProperties props;
    private final RedisService redisService;
    private final AuthService authService;

    private final String prefix = "users/avatar";

    @Override
    public List<UserResponseDto> findFriendsByUserId(UUID userId){
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("User id", userId));

        // Lấy tất cả lời mời đã chấp thuận với user là người gửi
        List<UserResponseDto> fromRequester = friendshipsRepository
                .findByRequesterAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendship::getReceiver)
                .map(UserResponseDto::from)
                .toList();

        // Lấy tất cả lời mời đã chấp thuận với user là người nhận
        List<UserResponseDto> fromReceiver = friendshipsRepository
                .findByReceiverAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendship::getRequester)
                .map(UserResponseDto::from)
                .toList();

        return Stream.concat(fromRequester.stream(), fromReceiver.stream()).toList();
    }

    @Override
    public boolean isFriendByUserId(UUID userId, UUID friendId) {
        List<UserResponseDto> friends = findFriendsByUserId(userId);

        for (UserResponseDto friend : friends){
            if (friend.id().equals(friendId))
                return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void updateAvatarByUserId(UUID userId, MultipartFile file) throws Exception {

        if (file == null || file.isEmpty())
            throw new BadRequestException("file", null);

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty())
            throw new IllegalArgumentException("Original filename is missing");

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("User id", userId));

        String objectName = FileUtil.getObjectNameFile(prefix, user.getId());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(props.getBucketName())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        user.setAvatarUrl(objectName);

        userRepository.save(user);
    }

    @Override
    public void updateUserLocationByUserId(UUID userId, Double latitude, Double longitude) {

        redisService.saveUserLocation(userId, latitude, longitude);
    }

    @Override
    public boolean isUserOnlineByUserId(UUID userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("User id", userId));

        LocalDateTime lastActiveAt = redisService
                .getUserLocation(user.getId())
                .lastActiveAt();

        if (lastActiveAt == null)
            return false;

        LocalDateTime now = LocalDateTime.now();

        return !lastActiveAt.isBefore(now.minusMinutes(3));
    }

    @Override
    public List<String> findFcmTokenOfFriendsByUserId(UUID userId){

        List<UserResponseDto> friends = findFriendsByUserId(userId);

        return friends.stream()
                .map(UserResponseDto::fcmToken)
                .toList();
    }

    @Override
    @Transactional
    public void updateDisplayNameByUserId(UUID userId, String displayName) {

        if (displayName == null || displayName.trim().isEmpty())
            throw new BadRequestException("displayName", displayName);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("User id", userId));

        user.setDisplayName(displayName);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateModeByUserId(UUID userId, UserMode mode) {

        if (mode == null)
            throw new BadRequestException("mode", null);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("User id", userId));

        user.setMode(mode);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateFcmTokenByUserId(UUID userId, String fcmToken) {

        if (fcmToken == null || fcmToken.trim().isEmpty())
            throw new BadRequestException("fcmToken", fcmToken);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("User id", userId));

        user.setFcmToken(fcmToken);

        userRepository.save(user);
    }

    @Override
    public InputStream getAvatar(UUID userId){

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return minIOService.getFile(user.getAvatarUrl());
    }

    @Override
    public List<UserSimpleResponseDto> searchFriend (String keyword) {
        User user = authService.getCurrentUser();

        return userRepository.searchStrangers(
                        user.getId(),
                        keyword,
                        PageRequest.of(0, 10)
                )
                .getContent()
                .stream()
                .map(UserSimpleResponseDto::from)
                // Lọc: chỉ giữ lại những user có ID KHÁC với ID của user hiện tại
                .filter(u -> !u.userId().equals(user.getId()))
                .toList();
    }
}
