package com.example.lockly.domain.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record LocationUserResponseDto (
        UUID userId,
        Double latitude,
        Double longitude,
        LocalDateTime lastActiveAt
){
    public static LocationUserResponseDto from (
            UUID userId,
            Double latitude,
            Double longitude,
            LocalDateTime lastActiveAt
    ){
        return new LocationUserResponseDto(
                userId,
                latitude,
                longitude,
                lastActiveAt
        );
    }
}
