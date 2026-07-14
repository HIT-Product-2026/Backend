package com.example.lockly.domain.dto.response;

import com.example.lockly.constant.CommonConstant;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LoginResponseDto{
        String accessToken;
        String refreshToken;
        UserResponseDto user;

        @Builder.Default
        String tokenType = CommonConstant.BEARER_TOKEN;
}
