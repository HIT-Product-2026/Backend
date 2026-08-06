package com.example.lockly.mapper.user;

import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.MinIOService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResponseMapper {

    private final MinIOService minIOService;

    public UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getMode(),
                minIOService.generatePresignedUrl(user.getObjectNameAvatar())
        );
    }
}
