package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponseDto(
    String id,
    String username,
    String email,
    String displayName,
    String avatarUrl,
    UserMode mode,
    String fcmToken
){

    public static UserResponseDto from(User user){
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getMode(),
                user.getFcmToken()
        );
    }
}
