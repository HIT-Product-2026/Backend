package com.example.lockly.domain.dto.request.create;

import com.example.lockly.domain.entity.main.enumEntity.TypePost;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.checkerframework.checker.units.qual.N;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequestDto (

        @NotNull
        MultipartFile file,

        @Size(max = 500)
        String caption,

        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude,

        @NotNull
        TypePost type
){
}