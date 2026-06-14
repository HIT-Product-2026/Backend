package com.example.lockly.domain.dto.response;

import com.example.lockly.constant.CommonConstant;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LoginResponseDto{
        String accessToken;
        String refreshToken;
        String id;

        @Builder.Default
        String tokenType = CommonConstant.BEARER_TOKEN;
}
