package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface UserService{

    List<UserResponseDto> findFriendsByUser(User user);
    List<String> findFcmTokenOfFriendsByUserId(User user);
    String getAvatar(User user);
    List<UserSimpleResponseDto> searchFriend(User user, String keywork);

    boolean isUserOnlineByUserId(User user);
    boolean isFriendByUserId(User user, UUID friendId);

    void updateDisplayNameByUserId(User user, String displayName);
    void updateAvatarByUserId(User user, MultipartFile file) throws Exception;
    void updateModeByUserId(User user, UserMode mode);
    void updateFcmTokenByUserId(User user, String fcmToken);
}