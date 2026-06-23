package com.example.lockly.service;

import com.example.lockly.domain.dto.request.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    public List<FriendshipsResponseDto> findFriendshipsByUserId(String id);
    public FriendshipsResponseDto acceptAddFriendRequest(String friendshipId);
    public FriendshipsResponseDto rejectAddFriendRequest(String friendshipId);
    public List<UserResponseDto> findFriendByUserId(String id);
    public FriendshipsResponseDto sendFriendshipRequest(CreateFriendshipRequestDto request);
    UserResponseDto findUserById(String id);
    void updateAvatar(MultipartFile file) throws Exception;
    public boolean isUserOnline();
    public void updateUserLocation(Double latitude, Double longitude);
    public List<String> findFcmTokenOfFriendsByUserId();
}