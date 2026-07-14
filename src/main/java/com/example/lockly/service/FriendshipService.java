package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {

    List<FriendshipsResponseDto> findFriendRequestRequesterByUserId(UUID id);
    List<FriendshipsResponseDto> findFriendRequestsReceivedByUserId(UUID userId);

    FriendshipsResponseDto acceptAddFriendRequest(UUID userId, UUID friendshipId);
    FriendshipsResponseDto rejectAddFriendRequest( UUID userId, UUID friendshipId);
    FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request);
    FriendshipsResponseDto unfriend(UUID userId ,UUID friendId);
}
