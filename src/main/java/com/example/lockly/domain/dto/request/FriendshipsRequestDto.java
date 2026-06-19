package com.example.lockly.domain.dto.request;

import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.FriendshipStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public record FriendshipsRequestDto(

        @Null(message = "Id phải để trống khi tạo lời mời kết bạn")
        String id,

        @NotNull(message = "Người gửi lời mời không được để trống")
        UserResponseDto requester,

        @NotNull(message = "Người nhận lời mời không được để trống")
        UserResponseDto receiver,

        @NotNull(message = "Trạng thái không được để trống")
        FriendshipStatus status
) {
}