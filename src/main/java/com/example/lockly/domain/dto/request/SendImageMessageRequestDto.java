package com.example.lockly.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record SendImageMessageRequestDto(

        @NotNull
        UUID conversationId,

        @NotNull
        MultipartFile file
) {
}