package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.entity.User;
import com.example.lockly.service.AuthService;
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

import java.util.UUID;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_V1 + "/friendship")
@Tag(name = "Friendship Controller", description = "API quản lý kết bạn")
public class FriendshipController {

    private final UserService userService;
    private final AuthService authService;

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


    @PostMapping("/accept/{friendships_id}")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> acceptFriendRequest(
            @PathVariable @Valid UUID friendshipsId
    ) {
        User user = authService.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        userService.acceptAddFriendRequest(user.getId(), friendshipsId)));
    }


    @PostMapping("/reject/{friendships_id}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> rejectFriendRequest(
            @PathVariable @Valid UUID friendshipId
    ) {
        User user = authService.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        userService.rejectAddFriendRequest(user.getId(), friendshipId)));
    }

}