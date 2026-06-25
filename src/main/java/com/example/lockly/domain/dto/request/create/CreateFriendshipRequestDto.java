package com.example.lockly.domain.dto.request.create;

import jakarta.validation.constraints.NotNull;

public record CreateFriendshipRequestDto (

    @NotNull(message = "Người gửi lời mời không được để trống")
    String requesterId,

    @NotNull(message = "Người nhận lời mời không được để trống")
    String receiverId
) {
    }