package com.example.lockly.domain.dto.request.create;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateFriendshipRequestDto (

    @NotNull(message = "Người gửi lời mời không được để trống")
    UUID requesterId,

    @NotNull(message = "Người nhận lời mời không được để trống")
    UUID receiverId
) {
    }