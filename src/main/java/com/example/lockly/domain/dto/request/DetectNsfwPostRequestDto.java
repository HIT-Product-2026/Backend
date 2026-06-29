package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.NsfwStatus;
import com.example.lockly.domain.entity.PostModeLocation;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record DetectNsfwPostRequestDto(
        UUID id,
        UserResponseDto user,
        String caption,
        String imageUrl,
        String contentType,
        Double latitude,
        Double longitude,
        PostModeLocation modeLocation,
        NsfwStatus nfws,
        MultipartFile file
) {
    public static DetectNsfwPostRequestDto from(PostResponseDto dto, MultipartFile file){
        return new DetectNsfwPostRequestDto(
                dto.id(),
                dto.user(),
                dto.caption(),
                dto.imageUrl(),
                dto.contentType(),
                dto.latitude(),
                dto.longitude(),
                dto.modeLocation(),
                dto.nfws(),
                file
        );
    }
}
