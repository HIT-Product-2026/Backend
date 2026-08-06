package com.example.lockly.mapper;

import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.MinIOService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserSimpleResponseMapper {

    public static MinIOService minIOService;

    public static UserSimpleResponseDto from(User user){
        return new UserSimpleResponseDto(
                user.getId(),
                user.getDisplayName(),
                minIOService.generatePresignedUrl(user.getObjectNameAvatar())
        );
    }
}
