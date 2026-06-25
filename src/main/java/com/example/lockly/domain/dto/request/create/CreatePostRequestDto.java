package com.example.lockly.domain.dto.request.create;

import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequestDto (
    MultipartFile file,
    String caption,
    Double latitude,
    Double longitude
){
}