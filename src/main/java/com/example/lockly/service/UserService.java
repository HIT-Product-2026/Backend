package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.UserMode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    UserDetails loadUserByUsername(String username);
    UserResponseDto getMyInfo(String username) throws UsernameNotFoundException;
    List<FriendshipsResponseDto> findFriendshipsByUserId(UUID id);
    FriendshipsResponseDto acceptAddFriendRequest(UUID userId, UUID friendshipId);
    FriendshipsResponseDto rejectAddFriendRequest( UUID userId, UUID friendshipId);
    List<UserResponseDto> findFriendsByUserId(UUID userId);
    FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request);
    UserResponseDto findUserById(UUID id);
    boolean isUserOnlineByUserId(UUID userId);
    void updateUserLocationByUserId(UUID userId, Double latitude, Double longitude);
    List<String> findFcmTokenOfFriendsByUserId(UUID userId);
    boolean isFriendByUserId(UUID userId, UUID friendId);

    void updateDisplayNameByUserId(UUID userId, String displayName);
    void updateAvatarByUserId(UUID userId, MultipartFile file) throws Exception;
    void updateModeByUserId(UUID userId, UserMode mode);
    void updateFcmTokenByUserId(UUID userId, String fcmToken);
}