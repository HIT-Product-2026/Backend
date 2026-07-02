package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.NsfwStatus;
import com.example.lockly.domain.entity.PostModeLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record DetectNsfwPostRequestDto(

        @NotNull
        UUID postId,

        @Valid
        @NotNull
        UserResponseDto user,

        @Size(max = 500)
        String caption,

        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude,

        @NotNull
        PostModeLocation modeLocation,

        @NotNull
        NsfwStatus nsfw,

        @NotNull
        MultipartFile file
) {
    public static DetectNsfwPostRequestDto from(PostResponseDto dto, MultipartFile file){
        return new DetectNsfwPostRequestDto(
                dto.id(),
                dto.user(),
                dto.caption(),
                dto.latitude(),
                dto.longitude(),
                dto.modeLocation(),
                dto.nsfw(),
                file
        );
    }
}