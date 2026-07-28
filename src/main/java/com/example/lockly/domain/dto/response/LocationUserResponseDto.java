package com.example.lockly.domain.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record LocationUserResponseDto (
        UUID userId,
        Double latitude,
        Double longitude,
        LocalDateTime lastActiveAt
){
}
