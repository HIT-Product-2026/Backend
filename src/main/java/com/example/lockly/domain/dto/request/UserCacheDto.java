package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;

import java.util.UUID;

public record UserCacheDto(
        UUID id,
        String email,
        UserMode mode
) {
    public static UserCacheDto from(User user){
        return new UserCacheDto(
                user.getId(),
                user.getEmail(),
                user.getMode()
        );
    }
}
