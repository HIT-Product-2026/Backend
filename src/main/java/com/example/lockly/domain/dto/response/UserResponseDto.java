package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.entity.User;
import lombok.Builder;

@Builder
public record UserResponseDto(
    String id,
    String username,
    String email
){

    public static UserResponseDto from(User user){
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
