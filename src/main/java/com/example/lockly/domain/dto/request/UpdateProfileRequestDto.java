package com.example.lockly.domain.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequestDto(
        @Past(message = "birthday must be in the past")
        LocalDate birthday,

        @Size(max = 500, message = "hobbies must not exceed 500 characters")
        String hobbies,

        @Size(max = 20, message = "phone number must not exceed 20 characters")
        String phoneNumber
) {
}
