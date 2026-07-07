package com.example.lockly.domain.dto.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateProfileRequestDto(
        @NotNull(message = "userId is required")
        UUID userId,

        @Past(message = "birthday must be in the past")
        LocalDate birthday,

        @Size(max = 500, message = "hobbies must not exceed 500 characters")
        String hobbies,

        @Size(max = 20, message = "phone number must not exceed 20 characters")
        String phoneNumber
) {
}
