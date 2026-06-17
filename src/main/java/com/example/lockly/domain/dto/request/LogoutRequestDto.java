package com.example.lockly.domain.dto.request;

import com.example.lockly.constant.ErrorMessage;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDto(
        @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
        String token
) {}
