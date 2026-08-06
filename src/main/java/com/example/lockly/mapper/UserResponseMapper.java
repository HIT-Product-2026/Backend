package com.example.lockly.mapper;

import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.MinIOService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserResponseMapper {

    public static MinIOService minIOService;

    public static UserResponseDto from(User user){
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getMode(),
                minIOService.generatePresignedUrl(user.getObjectNameAvatar())
        );
    }
}
