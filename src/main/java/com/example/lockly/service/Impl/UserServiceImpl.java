package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.minio.MinioProperties;
import com.example.lockly.domain.dto.request.UserCacheDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.domain.entity.main.Friendship;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.repository.main.FriendshipsRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.*;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FriendshipsRepository friendshipsRepository;
    private final MinIOService minIOService;
    private final MinioClient minioClient;
    private final MinioProperties props;
    private final RedisService redisService;

    private final String prefix = "users/avatar";

    @Override
    public List<UserResponseDto> findFriendsByUser(User user){

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
    public boolean isFriendByUserId(User user, UUID friendId) {
        List<UserResponseDto> friends = findFriendsByUser(user);

        for (UserResponseDto friend : friends){
            if (friend.id().equals(friendId))
                return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void updateAvatarByUserId(User userRequest, MultipartFile file) throws Exception {

        log.info("Bắt đầu câ nhật avt");

        User user = userRepository
                .findById(userRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userRequest.getId()));

        if (file == null || file.isEmpty())
            throw new BadRequestException("file", null);

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty())
            throw new IllegalArgumentException("Original filename is missing");

        log.info("Bắt đầu tạo object name");

        String objectName = FileUtil.getObjectNameFile(prefix, user.getId());

        log.info("Bắt đầu lưu vào minIO");

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(props.getBucketName())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        log.info("Bắt đầu set vào User");

        user.setObjectNameAvatar(objectName);

        log.info("Bắt đầu lưu vào db");

        userRepository.save(user);

        redisService.saveUser(UserCacheDto.from(user));

        log.info("Lưu thành công với object name: " + objectName);

        log.info("Update avt, avt find is: " + user.getObjectNameAvatar());

    }

    @Override
    public boolean isUserOnlineByUserId(User user) {

        LocalDateTime lastActiveAt = redisService
                .getUserLocation(user.getId())
                .lastActiveAt();

        if (lastActiveAt == null)
            return false;

        LocalDateTime now = LocalDateTime.now();

        return !lastActiveAt.isBefore(now.minusMinutes(3));
    }

    @Override
    public List<String> findFcmTokenOfFriendsByUserId(User user){

        // Lấy tất cả lời mời đã chấp thuận với user là người gửi
        List<User> fromRequester = friendshipsRepository
                .findByRequesterAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendship::getReceiver)
                .toList();

        // Lấy tất cả lời mời đã chấp thuận với user là người nhận
        List<User> fromReceiver = friendshipsRepository
                .findByReceiverAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendship::getRequester)
                .toList();


        List<User> friends = Stream.concat(fromRequester.stream(), fromReceiver.stream()).toList();

        friends.forEach(friend ->
                log.info(
                        "Friend: id={}, name={}",
                        friend.getId(),
                        friend.getDisplayName()
                )
        );

        return friends.stream()
                .map(User::getFcmToken)
                .toList();
    }

    @Override
    @Transactional
    public void updateDisplayNameByUserId(User userCache, String displayName) {

        User user = userRepository
                .findById(userCache.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userCache.getId()));

        log.info(user.getObjectNameAvatar());

        if (displayName == null || displayName.trim().isEmpty())
            throw new BadRequestException("displayName", displayName);

        user.setDisplayName(displayName);

        userRepository.save(user);

        redisService.saveUser(UserCacheDto.from(user));

        log.info("Update display name, avt find is: " + user.getObjectNameAvatar());
    }

    @Override
    @Transactional
    public void updateModeByUserId(User user, UserMode mode) {

        if (mode == null)
            throw new BadRequestException("mode", null);

        user.setMode(mode);

        userRepository.save(user);

        redisService.saveUser(UserCacheDto.from(user));
    }

    @Override
    @Transactional
    public void updateFcmTokenByUserId(User user, String fcmToken) {

        if (fcmToken == null || fcmToken.trim().isEmpty())
            throw new BadRequestException("fcmToken", fcmToken);

        user.setFcmToken(fcmToken);

        userRepository.save(user);

        redisService.saveUser(UserCacheDto.from(user));
    }

    @Override
    public String getAvatar(User user){

        if (user.getObjectNameAvatar() == null)
            return null;

        return minIOService.generatePresignedUrl(user.getObjectNameAvatar());
    }

    @Override
    public List<UserSimpleResponseDto> searchFriend (User user, String keyword) {

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
