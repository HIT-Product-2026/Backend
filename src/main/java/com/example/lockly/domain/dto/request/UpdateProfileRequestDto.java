package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.entity.main.enumEntity.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequestDto(
        @Pattern(
                regexp = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$",
                message = "Birthday must be in format dd/MM/yyyy"
        )
        String birthday,

        @Size(message = "hobbies must not exceed 500 characters")
        Gender gender,

        @Size(max = 20, message = "phone number must not exceed 20 characters")
        String phoneNumber
) {
}
