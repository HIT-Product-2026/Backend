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
    FriendshipsResponseDto acceptAddFriendRequest(String userId, String friendshipId);
    FriendshipsResponseDto rejectAddFriendRequest( String userId, String friendshipId);
    List<UserResponseDto> findFriendsByUserId(String userId);
    FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request);
    UserResponseDto findUserById(String id);
    boolean isUserOnlineByUserId(String userId);
    void updateUserLocationByUserId(String userId, Double latitude, Double longitude);
    List<String> findFcmTokenOfFriendsByUserId(String userId);
    boolean isFriendByUserId(String userId, String friendId);

    void updateDisplayNameByUserId(String userId, String displayName);
    void updateAvatarByUserId(String userId, MultipartFile file) throws Exception;
    void updateModeByUserId(String userId, UserMode mode);
    void updateFcmTokenByUserId(String userId, String fcmToken);
}