package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.dto.response.common.PostResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostCursor(
        LocalDateTime createdAt,
        UUID id
) {
    public static PostCursor from(PostResponseDto dto){
        return new PostCursor(
                dto.createAt(),
                dto.id()
        );
    }
}
