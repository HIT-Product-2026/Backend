package com.example.lockly.service;

import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    public UserDetails loadUserByUsername(String username);
    public List<FriendshipsResponseDto> findAllFriendshipsByUserId(String id);
    public FriendshipsResponseDto acceptAddFriendRequest(FriendshipsRequestDto request);
    public FriendshipsResponseDto rejectAddFriendRequest(FriendshipsRequestDto request);
    public List<UserResponseDto> findAllListFriendByUserId(String id);
    public FriendshipsResponseDto sendFriendshipRequest(FriendshipsRequestDto request);
    UserResponseDto getUserById(String id);
    void updateAvatar(String userId, MultipartFile file) throws Exception;
}