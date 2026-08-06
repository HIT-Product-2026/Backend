package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserCacheDto(
        UUID id,
        String username,
        String displayName,
        String email,
        String passwordHash,
        String objectNameAvatar,
        UserMode mode,
        LocalDateTime createdAt
) implements Serializable {

    public static UserCacheDto from(User user) {
        return new UserCacheDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getObjectNameAvatar(),
                user.getMode(),
                user.getCreatedAt()
        );
    }

    public User toEntity() {
        User user = new User();

        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setObjectNameAvatar(objectNameAvatar);
        user.setMode(mode);
        user.setCreatedAt(createdAt);

        return user;
    }
}