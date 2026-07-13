package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            @Parameter(description = "Id người nhận")
            @RequestParam("receiverId") UUID receiverId
    ) {
        User user = authService.getCurrentUser();

        CreateFriendshipRequestDto request = new CreateFriendshipRequestDto(
                user.getId(),
                receiverId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thành công",
                        userService.sendFriendshipRequest(request)));
    }


    @PostMapping("/accept/{friendships_id}")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> acceptFriendRequest(
            @PathVariable("friendships_id") UUID friendshipId
    ){
        User user = authService.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        userService.acceptAddFriendRequest(user.getId(), friendshipId)));
    }


    @PostMapping("/reject/{friendships_id}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> rejectFriendRequest(
            @PathVariable("friendships_id") UUID friendshipId
    ) {
        User user = authService.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        userService.rejectAddFriendRequest(user.getId(), friendshipId)));
    }

    @PostMapping("/reject/{friendships_id}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> unfriendRequest(
            @PathVariable("friendships_id") UUID friendshipId
    ) {
        User user = authService.getCurrentUser();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        userService.unfriend(user.getId(), friendshipId)));
    }

    @GetMapping("/friends")
    @Operation(summary = "Lấy danh sách bạn bè", description = "Trả về danh sách bạn bè của user")
    public ResponseEntity<ApiResponse<ListResponse<UserResponseDto>>> getFriends() {
        User user = authService.getCurrentUser();
        List<UserResponseDto> listFriend = userService.findFriendsByUserId(user.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(listFriend)));
    }

    @GetMapping("/friendships/requester")
    @Operation(summary = "Lấy danh sách lời mời kết bạn", description = "Trả về danh sách lời mời kết bạn (PENDING)")
    public ResponseEntity<ApiResponse<ListResponse<FriendshipsResponseDto>>> getFriendRequests() {
        User user = authService.getCurrentUser();

        List<FriendshipsResponseDto> result = userService.findFriendRequestRequesterByUserId(user.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }

    @GetMapping("/friendships/received")
    @Operation(
            summary = "Lấy danh sách lời mời kết bạn đã nhận",
            description = "Trả về danh sách các lời mời kết bạn mà người dùng hiện tại là người nhận"
    )
    public ResponseEntity<ApiResponse<ListResponse<FriendshipsResponseDto>>> getReceivedFriendRequests() {

        User user = authService.getCurrentUser();

        List<FriendshipsResponseDto> result =
                userService.findFriendRequestsReceivedByUserId(user.getId());

        return ResponseEntity
                .ok(ApiResponse.success("Thành công", ListResponse.of(result)));
    }
}