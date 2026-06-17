package com.example.lockly.service.Impl;

import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendships;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.FriendshipsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    FriendshipsRepository friendshipsRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findUserDetailByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorMessage.User.ERR_USER_NOT_EXISTED + username));
        return new CustomUserDetails(user);
    }

    public List<FriendshipsResponseDto> findAllFriendshipsByUserId(String id){
        User requester = userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        List<Friendships> friendshipsList = friendshipsRepository
                .findByRequesterAndStatus(requester, FriendshipStatus.PENDING);

        List<FriendshipsResponseDto> friendshipsDtoList = friendshipsList.stream()
                .map(FriendshipsResponseDto::from)
                .toList();

        return friendshipsDtoList;
    }

    public FriendshipsResponseDto acceptAddFriendRequest(FriendshipsRequestDto request){
        Friendships friendships = friendshipsRepository
                .findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", request.id()));

        // Lời mời kết bạn phải ở trạng thái PEDDING mới có thể đồng ý
        if (friendships.getStatus() != FriendshipStatus.PENDING)
            throw new BadRequestException("status", request.status().name());

        friendships.setStatus(FriendshipStatus.ACCEPTED);

        return FriendshipsResponseDto.from(
                friendshipsRepository.save(friendships)
        );
    }

    public FriendshipsResponseDto rejectAddFriendRequest(FriendshipsRequestDto request){
        Friendships friendships = friendshipsRepository
                .findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", request.id()));

        // Từ chối kết bạn phải ở trạng thái PEDDING mới có thể từ chối
        if (friendships.getStatus() != FriendshipStatus.PENDING)
            throw new BadRequestException("status", request.status().name());

        friendships.setStatus(FriendshipStatus.REJECTED);

        friendshipsRepository.delete(friendships);

        return FriendshipsResponseDto.from(friendships);
    }

    public List<UserResponseDto> findAllListFriendByUserId(String id){
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Lấy tất cả lời mời đã chấp thuận với user là người gửi
        List<UserResponseDto> fromRequester = friendshipsRepository
                .findByRequesterAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendships::getReceiver)
                .map(UserResponseDto::from)
                .toList();

        // Lấy tất cả lời mời đã chấp thuận với user là người nhận
        List<UserResponseDto> fromReceiver = friendshipsRepository
                .findByReceiverAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(Friendships::getRequester)
                .map(UserResponseDto::from)
                .toList();

        return Stream.concat(fromRequester.stream(), fromReceiver.stream()).toList();
    }

    public FriendshipsResponseDto sendFriendshipRequest(FriendshipsRequestDto request){
        User requester = userRepository
                .findById(request.requester().id())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.requester().id()));
        User receiver = userRepository
                .findById(request.receiver().id())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.receiver().id()));

        Friendships friendships = Friendships.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        return FriendshipsResponseDto.from(
                friendshipsRepository.save(friendships)
        );
    }
}
