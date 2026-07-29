package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.entity.main.User;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {

    List<FriendshipsResponseDto> findFriendRequestRequesterByUserId(User requester);
    List<FriendshipsResponseDto> findFriendRequestsReceivedByUserId(User receiver);

    FriendshipsResponseDto acceptAddFriendRequest(UUID userId, UUID friendshipId);
    FriendshipsResponseDto rejectAddFriendRequest( UUID userId, UUID friendshipId);
    FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request);
    FriendshipsResponseDto unfriend(UUID userId ,UUID friendId);
}
