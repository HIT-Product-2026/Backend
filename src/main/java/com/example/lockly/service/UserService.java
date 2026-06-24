package com.example.lockly.service;

import com.example.lockly.domain.dto.request.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.UserMode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    List<FriendshipsResponseDto> findFriendshipsByUserId(String id);
    FriendshipsResponseDto acceptAddFriendRequest(String friendshipId);
    FriendshipsResponseDto rejectAddFriendRequest(String friendshipId);
    List<UserResponseDto> findFriends();
    FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request);
    UserResponseDto findUserById(String id);
    boolean isUserOnline();
    void updateUserLocation(Double latitude, Double longitude);
    List<String> findFcmTokenOfFriends();
    boolean isFriend(String friendId);

    void updateDisplayName(String displayName);
    void updateAvatar(MultipartFile file) throws Exception;
    void updateMode(UserMode mode);
    void updateFcmToken(String fcmToken);
}