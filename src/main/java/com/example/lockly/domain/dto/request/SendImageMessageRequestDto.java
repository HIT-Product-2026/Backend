package com.example.lockly.domain.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record SendImageMessageRequestDto(
        String conversationId,
        String senderId,
        MultipartFile file
) {
}
