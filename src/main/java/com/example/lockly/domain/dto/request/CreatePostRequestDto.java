package com.example.lockly.domain.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequestDto (
    MultipartFile file,
    String userId,
    String caption,
    Double latitude,
    Double longitude
){
}