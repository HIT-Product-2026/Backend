package com.example.lockly.domain.dto.response;

public record NsfwResponse(
        Boolean nsfw,
        Double score
) {
}
