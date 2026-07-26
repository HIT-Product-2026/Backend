package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.dto.response.common.MessageResponseDto;

import java.util.List;

public record MessagePageResponse(
        List<MessageResponseDto> messages,
        String nextCursor
) {

    public static MessagePageResponse from(List<MessageResponseDto> messages, String nextCursor) {
        return new MessagePageResponse(
                messages,
                nextCursor
        );
    }
}