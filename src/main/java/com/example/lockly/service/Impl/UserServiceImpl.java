package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.request.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FcmPostResponseDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendship;
import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.DuplicateResourceException;
import com.example.lockly.exception.ForbiddenException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.FriendshipsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.RedisService;
import com.example.lockly.service.UserService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    FriendshipsRepository friendshipsRepository;
    MinioClient minioClient;
    MinioProperties props;
    AuthService authService;
    RedisService redisService;
    String prefix = "users/avatar";

    @Override
    public List<FriendshipsResponseDto> findFriendshipsByUserId(String id){
        User requester = userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        List<Friendship> friendshipList = friendshipsRepository
                .findByRequesterAndStatus(requester, FriendshipStatus.PENDING);

        List<FriendshipsResponseDto> friendshipsDtoList = friendshipList
                .stream()
                .map(FriendshipsResponseDto::from)
                .toList();

        return friendshipsDtoList;
    }

        @Override
        @Transactional
        public FriendshipsResponseDto acceptAddFriendRequest(String friendshipId){
            Friendship friendship = friendshipsRepository
                    .findById(friendshipId)
                    .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", friendshipId));

            // Lời mời kết bạn phải ở trạng thái PENDING mới có thể đồng ý
            if (friendship.getStatus() != FriendshipStatus.PENDING)
                throw new BadRequestException("status", friendship.getStatus().name());

            User user = authService.getCurrentUser();

            // Chỉ người nhận lời mời mới có thể chấp nhận lời mời
            if (!friendship.getReceiver().getId().equals(user.getId()))
                throw new ForbiddenException("You are not allowed to accept this friend request");

            friendship.setStatus(FriendshipStatus.ACCEPTED);

            return FriendshipsResponseDto.from(friendship);
    }

    @Override
    @Transactional
    public FriendshipsResponseDto rejectAddFriendRequest(String friendshipId){
        Friendship friendship = friendshipsRepository
                .findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", friendshipId));

        // Từ chối kết bạn phải ở trạng thái PEDDING mới có thể từ chối
        if (friendship.getStatus() != FriendshipStatus.PENDING)
            throw new BadRequestException("Friendship is not PENDING");

        User user = authService.getCurrentUser();

        if (!friendship.getReceiver().getId().equals(user.getId())
        && friendship.getRequester().getId().equals(user.getId())){
            throw new ForbiddenException("You are not allowed to accept this friend request");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);

        friendshipsRepository.delete(friendship);

        return FriendshipsResponseDto.from(friendship);
    }

    @Override
    public List<UserResponseDto> findFriends(){
        User user = authService.getCurrentUser();

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
    public boolean isFriend(String friendId) {
        List<UserResponseDto> friends = findFriends();

        for (UserResponseDto friend : friends){
            if (friend.id().equals(friendId))
                return true;
        }
        return false;
    }

    @Override
    @Transactional
    public FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request){

        User requester = userRepository
                .findById(request.requesterId())
                .orElseThrow(() -> new BadRequestException("requester id", request.requesterId()));

        User receiver = userRepository
                .findById(request.receiverId())
                .orElseThrow(() -> new BadRequestException("receiver id", request.receiverId()));

        if (friendshipsRepository.existsByRequesterAndReceiver(requester, receiver)
        || friendshipsRepository.existsByReceiverAndRequester(receiver, requester))
            throw new DuplicateResourceException("Friend request has been sent");

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        return FriendshipsResponseDto.from(
                friendshipsRepository.save(friendship)
        );
    }

    @Override
    public UserResponseDto findUserById(String id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return UserResponseDto.from(user);
    }

    @Override
    @Transactional
    public void updateAvatar(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty())
            throw new BadRequestException("file", null);

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty())
            throw new IllegalArgumentException("Original filename is missing");

        User user = authService.getCurrentUser();

        String objectName = FileUtil.getObjectNameFile(prefix, user.getId(), file);

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
    @Transactional
    public void updateUserLocation(Double latitude, Double longitude) {

        User user = authService.getCurrentUser();

        redisService.saveUserLocation(user.getId(), latitude, longitude);

        userRepository.save(user);
    }

    @Override
    public boolean isUserOnline() {

        User user = authService.getCurrentUser();

        LocalDateTime lastActiveAt = redisService
                .getUserLocation(user.getId())
                .lastActiveAt();

        if (lastActiveAt == null)
            return false;

        LocalDateTime now = LocalDateTime.now();

        return !lastActiveAt.isBefore(now.minusMinutes(3));
    }

    @Override
    public List<String> findFcmTokenOfFriends(){

        List<UserResponseDto> friends = findFriends();

        return friends.stream()
                .map(UserResponseDto::fcmToken)
                .toList();
    }

    @Override
    @Transactional
    public void updateDisplayName(String displayName) {

        if (displayName == null || displayName.trim().isEmpty())
            throw new BadRequestException("displayName", displayName);

        User user = authService.getCurrentUser();

        user.setDisplayName(displayName);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateMode(UserMode  mode) {

        if (mode == null)
            throw new BadRequestException("mode", null);

        User user = authService.getCurrentUser();

        user.setMode(mode);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateFcmToken(String fcmToken) {

        if (fcmToken == null || fcmToken.trim().isEmpty())
            throw new BadRequestException("fcmToken", fcmToken);

        User user = authService.getCurrentUser();

        user.setFcmToken(fcmToken);

        userRepository.save(user);
    }
}
