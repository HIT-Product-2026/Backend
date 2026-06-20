package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.User;
import lombok.Builder;

@Builder
public record UserResponseDto(
        String username,
        String displayName
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getUsername(),
                user.getDisplayName()
        );
    }
}