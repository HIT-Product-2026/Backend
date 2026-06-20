package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping(ApiPath.API_V1 + "/friendships")
@Tag(name = "Friendship Controller", description = "API quản lý kết bạn")
public class FriendshipController {

    UserService userService;

    @PostMapping("/request")
    @Operation(summary = "Gửi lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> sendFriendRequest(
            @RequestBody @Valid CreateFriendshipRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thành công",
                        userService.sendFriendshipRequest(request)));
    }


    @PostMapping("/accept")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> acceptFriendRequest(
            @RequestBody @Valid FriendshipsRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        userService.acceptAddFriendRequest(request)));
    }


    @PostMapping("/reject")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> rejectFriendRequest(
            @RequestBody @Valid FriendshipsRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        userService.rejectAddFriendRequest(request)));
    }

}