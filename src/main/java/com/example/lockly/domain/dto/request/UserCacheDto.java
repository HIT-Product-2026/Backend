package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;

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
