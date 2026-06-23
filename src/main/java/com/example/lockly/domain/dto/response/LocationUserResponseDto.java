package com.example.lockly.domain.dto.response;

import java.time.LocalDateTime;

public record LocationUserResponseDto (
        Double latitude,
        Double longitude,
        LocalDateTime lastActiveAt
){
}
