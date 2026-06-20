package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.User;
<<<<<<< HEAD
import com.example.lockly.domain.entity.UserRole;
=======
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import lombok.Builder;

@Builder
public record UserResponseDto(
<<<<<<< HEAD
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
=======
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
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
