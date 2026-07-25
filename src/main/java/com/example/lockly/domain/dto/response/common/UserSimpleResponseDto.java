package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.User;

import java.util.UUID;

public record UserSimpleResponseDto (
        UUID userId,
        String displayName
){
    public static UserSimpleResponseDto from(User user){
        return new UserSimpleResponseDto(
                user.getId(),
                user.getDisplayName()
        );
    }
}
