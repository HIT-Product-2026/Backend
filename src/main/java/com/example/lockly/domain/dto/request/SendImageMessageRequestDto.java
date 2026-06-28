package com.example.lockly.domain.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record SendImageMessageRequestDto(
        UUID conversationId,
        MultipartFile file
) {
}
