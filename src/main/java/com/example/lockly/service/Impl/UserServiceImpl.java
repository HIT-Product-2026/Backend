package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.domain.dto.request.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendship;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.DuplicateResourceException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.FriendshipsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.service.UserService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    FriendshipsRepository friendshipsRepository;
    MinioClient minioClient;
    MinioProperties props;
    String prefix = "users/avatar";
    String prefixApi = "user";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findUserDetailByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorMessage.User.ERR_USER_NOT_EXISTED + username));
        return new CustomUserDetails(user);
    }

    @Override
    public List<FriendshipsResponseDto> findAllFriendshipsByUserId(String id){
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
    public FriendshipsResponseDto acceptAddFriendRequest(FriendshipsRequestDto request){
        Friendship friendship = friendshipsRepository
                .findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", request.id()));

        // Lời mời kết bạn phải ở trạng thái PEDDING mới có thể đồng ý
        if (friendship.getStatus() != FriendshipStatus.PENDING)
            throw new BadRequestException("status", request.status().name());

        friendship.setStatus(FriendshipStatus.ACCEPTED);

        return FriendshipsResponseDto.from(
                friendshipsRepository.save(friendship)
        );
    }

    @Override
    @Transactional
    public FriendshipsResponseDto rejectAddFriendRequest(FriendshipsRequestDto request){
        Friendship friendship = friendshipsRepository
                .findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", request.id()));

        // Từ chối kết bạn phải ở trạng thái PEDDING mới có thể từ chối
        if (friendship.getStatus() != FriendshipStatus.PENDING)
            throw new BadRequestException("Friendship is not PENDING");

        friendship.setStatus(FriendshipStatus.REJECTED);

        friendshipsRepository.delete(friendship);

        return FriendshipsResponseDto.from(friendship);
    }

    @Override
    public List<UserResponseDto> findAllListFriendByUserId(String id){
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

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
    @Transactional
    public FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request){

        User requester = userRepository
                .findById(request.requester().id())
                .orElseThrow(() -> new BadRequestException("requester id", request.requester().id()));

        User receiver = userRepository
                .findById(request.receiver().id())
                .orElseThrow(() -> new BadRequestException("receiver id", request.receiver().id()));

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
    public UserResponseDto getUserById(String id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return UserResponseDto.from(user);
    }

    @Override
    @Transactional
    public void updateAvatar(String userId, MultipartFile file) throws Exception {

        if (file == null || file.isEmpty())
            throw new BadRequestException("file", null);

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty())
            throw new IllegalArgumentException("Original filename is missing");

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String objectName = FileUtil.getObjectNameFile(prefix, userId, file);

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
    public void updateUserLocation(String userId, Double latitude, Double longitude) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setLatitude(latitude);
        user.setLongitude(longitude);

        userRepository.save(user);
    }

    @Override
    public boolean isUserOnline(String userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getLastActiveAt() == null)
            return false;

        LocalDateTime now = LocalDateTime.now();

        return !user.getLastActiveAt().isBefore(now.minusMinutes(5));
    }
}
