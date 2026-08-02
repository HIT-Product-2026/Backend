package com.example.lockly.domain.dto.response.common;

import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.enumEntity.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponseDto(
        UUID id,
        UUID userId,
        LocalDate birthday,
        Gender gender,
        String phoneNumber
) {
    public static ProfileResponseDto from(Profile profile) {
        return new ProfileResponseDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getBirthday(),
                profile.getGender(),
                profile.getPhoneNumber()
        );
    }
}
