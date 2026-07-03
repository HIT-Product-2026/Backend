package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.enumEntity.UserMode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String username);
    List<FriendshipsResponseDto> findFriendshipsByUserId(UUID id);
    List<UserResponseDto> findFriendsByUserId(UUID userId);
    List<String> findFcmTokenOfFriendsByUserId(UUID userId);
    InputStream getAvatar(UUID userId) throws Exception;
    UserResponseDto findUserById(UUID id);


    FriendshipsResponseDto acceptAddFriendRequest(UUID userId, UUID friendshipId);
    FriendshipsResponseDto rejectAddFriendRequest( UUID userId, UUID friendshipId);
    FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request);

    boolean isUserOnlineByUserId(UUID userId);
    boolean isFriendByUserId(UUID userId, UUID friendId);

    void updateUserLocationByUserId(UUID userId, Double latitude, Double longitude);
    void updateDisplayNameByUserId(UUID userId, String displayName);
    void updateAvatarByUserId(UUID userId, MultipartFile file) throws Exception;
    void updateModeByUserId(UUID userId, UserMode mode);
    void updateFcmTokenByUserId(UUID userId, String fcmToken);
}