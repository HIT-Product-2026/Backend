package com.example.lockly.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(

        @NotBlank(message = "Token không được để trống")
        String token

) {}