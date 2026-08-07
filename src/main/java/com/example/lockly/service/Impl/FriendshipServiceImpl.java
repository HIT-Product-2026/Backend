package com.example.lockly.service.Impl;

import com.example.lockly.domain.dto.request.create.CreateConversationRequestDto;
import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.Friendship;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.DuplicateResourceException;
import com.example.lockly.exception.nonRetryException.ForbiddenException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.mapper.friendship.FriendshipsResponseMapper;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.repository.main.FriendshipsRepository;
import com.example.lockly.repository.main.UserBlockRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.ConversationService;
import com.example.lockly.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipsRepository friendshipsRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;
    private final FriendshipsResponseMapper friendshipsResponseMapper;
    private final UserBlockRepository userBlockRepository;

    @Override
    public List<FriendshipsResponseDto> findFriendRequestRequesterByUserId(User requester) {

        List<Friendship> friendshipList = friendshipsRepository
                .findByRequesterAndStatus(requester, FriendshipStatus.SENT);

        return friendshipList
                .stream()
                .map(friendshipsResponseMapper::from)
                .toList();
    }

    @Override
    public List<FriendshipsResponseDto> findFriendRequestsReceivedByUserId(User receiver) {

        List<Friendship> friendshipList = friendshipsRepository
                .findByReceiverAndStatus(receiver, FriendshipStatus.SENT);

        return friendshipList.stream()
                .map(friendshipsResponseMapper::from)
                .toList();
    }

    @Override
    @Transactional
    public FriendshipsResponseDto acceptAddFriendRequest(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipsRepository
                .findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", friendshipId));

        // Lời mời kết bạn phải ở trạng thái PENDING mới có thể đồng ý
        if (friendship.getStatus() != FriendshipStatus.SENT)
            throw new BadRequestException("status", friendship.getStatus().name());

        // Tạo cuộc hôi thoại khi kết bạn
        conversationService.createConversation(new CreateConversationRequestDto(
                        userId,
                        friendship.getRequester().getId()
                )
        );

        // Chỉ người nhận lời mời mới có thể chấp nhận lời mời
        if (!friendship.getReceiver().getId().equals(userId))
            throw new ForbiddenException("You are not allowed to accept this friend request");

        friendship.setStatus(FriendshipStatus.ACCEPTED);

        friendshipsRepository.save(friendship);

        return friendshipsResponseMapper.from(friendship);
    }

    @Override
    public FriendshipsResponseDto rejectAddFriendRequest(UUID userId, UUID friendshipId) {
        Friendship friendship = friendshipsRepository
                .findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "id", friendshipId));

        // Từ chối kết bạn phải ở trạng thái PEDDING mới có thể từ chối
        if (friendship.getStatus() != FriendshipStatus.SENT)
            throw new BadRequestException("Friendship is not PENDING");

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new BadRequestException("User id", userId));

        if (!friendship.getReceiver().getId().equals(user.getId())
                && !friendship.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not allowed to accept this friend request");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);

        friendshipsRepository.delete(friendship);

        return friendshipsResponseMapper.from(friendship);
    }

    @Override
    @Transactional
    public FriendshipsResponseDto unfriend(UUID userId, UUID friendId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend", "id", friendId));

        Friendship friendship = friendshipsRepository
                .findFriendshipBetweenUsers(user, friend)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship", "friendId", friendId));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new BadRequestException("Friendship is not ACCEPTED");
        }

        // Xóa cuộc hội thoại (nếu có)
        Conversation conversation = conversationRepository.findByUsers(
                user,
                friend
        ).orElse(null);

        friendshipsRepository.delete(friendship);

        if (conversation != null) {
            conversationRepository.delete(conversation);
        }

        return friendshipsResponseMapper.from(friendship);
    }


    @Override
    @Transactional
    public FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request) {

        User requester = userRepository
                .findById(request.requesterId())
                .orElseThrow(() -> new BadRequestException("requester id", request.requesterId()));

        User receiver = userRepository
                .findById(request.receiverId())
                .orElseThrow(() -> new BadRequestException("receiver id", request.receiverId()));

        // Người gửi và người nhận không được cùng là 1 người
        if (requester.getId().equals(receiver.getId()))
            throw new BadRequestException("Người gửi và người nhận không được trùng nhau");

        if (userBlockRepository.existsBlockBetweenUsers(requester, receiver)) {
            throw new ForbiddenException("Không thể gửi lời mời kết bạn");
        }
        // Không được kết bạn với người đã là bạn
        if (friendshipsRepository.existsByRequesterAndReceiver(requester, receiver)
                || friendshipsRepository.existsByRequesterAndReceiver(receiver, requester)) {
            throw new DuplicateResourceException("Friend request has been sent");
        }

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendshipStatus.SENT)
                .build();

        friendshipsRepository.save(friendship);

        return friendshipsResponseMapper.from(
                friendshipsRepository.save(friendship)
        );
    }

}
