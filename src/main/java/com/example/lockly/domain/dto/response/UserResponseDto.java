package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.User;
import lombok.Builder;

@Builder
public record UserResponseDto(
    String id,
    String username,
    String email,
    String displayName,
    String avatarUrl,
    Double latitude,
    Double longitude
){

    public static UserResponseDto from(User user){
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getLatitude(),
                user.getLongitude()
        );
    }
}
