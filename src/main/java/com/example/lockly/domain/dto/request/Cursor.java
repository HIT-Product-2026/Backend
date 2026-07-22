package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.Message;

import java.time.LocalDateTime;
import java.util.UUID;

public record Cursor(
        LocalDateTime createdAt,
        UUID id
) {
    public static Cursor from(PostResponseDto dto){
        return new Cursor(
                dto.createAt(),
                dto.id()
        );
    }

    public static Cursor from(Message message){
        return new Cursor(
                message.getCreatedAt(),
                message.getId()
        );
    }
}
