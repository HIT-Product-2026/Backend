package com.example.lockly.domain.dto.response;

import com.example.lockly.domain.dto.response.common.MessageResponseDto;
import com.example.lockly.domain.entity.main.Message;

import java.util.List;

public record MessagePageResponse(
        List<MessageResponseDto> messages,
        String nextCursor
) {

    public static MessagePageResponse from(
            List<Message> messages,
            String nextCursor
    ) {
        return new MessagePageResponse(
                messages.stream()
                        .map(MessageResponseDto::from)
                        .toList(),
                nextCursor
        );
    }
}