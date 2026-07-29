package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.FriendshipService;
import com.example.lockly.service.RedisService;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_NOW + "/friendship")
@Tag(name = "Friendship Controller", description = "API quản lý kết bạn")
public class FriendshipController {

    private final UserService userService;
    private final AuthService authService;
    private final FriendshipService friendshipService;
    private final RedisService redisService;

    @PostMapping("/request")
    @Operation(summary = "Gửi lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> sendFriendRequest(
            @Parameter(description = "Id người nhận")
            @RequestParam("receiverId") UUID receiverId
    ) {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        CreateFriendshipRequestDto request = new CreateFriendshipRequestDto(
                user.getId(),
                receiverId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thành công",
                        friendshipService.sendFriendshipRequest(request)));
    }


    @PostMapping("/accept/{friendships_id}")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> acceptFriendRequest(
            @PathVariable("friendships_id") UUID friendshipId
    ){
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        friendshipService.acceptAddFriendRequest(user.getId(), friendshipId)));
    }


    @PostMapping("/reject/{friendships_id}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> rejectFriendRequest(
            @PathVariable("friendships_id") UUID friendshipId
    ) {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        friendshipService.rejectAddFriendRequest(user.getId(), friendshipId)));
    }

    @PostMapping("/unfriend/{friendId}")
    @Operation(summary = "Xóa kết bạn")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> unfriendRequest(
            @PathVariable("friendId") UUID friendId
    ) {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công",
                        friendshipService.unfriend(user.getId(), friendId)));
    }

    @GetMapping("/friends")
    @Operation(summary = "Lấy danh sách bạn bè", description = "Trả về danh sách bạn bè của user")
    public ResponseEntity<ApiResponse<ListResponse<UserResponseDto>>> getFriends() {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();
        List<UserResponseDto> listFriend = userService.findFriendsByUser(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(listFriend)));
    }

    @GetMapping("/friendships/requester")
    @Operation(summary = "Lấy danh sách lời mời kết bạn", description = "Trả về danh sách lời mời kết bạn (PENDING)")
    public ResponseEntity<ApiResponse<ListResponse<FriendshipsResponseDto>>> getFriendRequests() {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        List<FriendshipsResponseDto> result = friendshipService
                .findFriendRequestRequesterByUserId(user);

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

        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        List<FriendshipsResponseDto> result =
                friendshipService.findFriendRequestsReceivedByUserId(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }

    @GetMapping("/friendships/search")
    @Operation(
            summary = "Tìm kiếm người dùng để kết bạn",
            description = "Tìm kiếm người dùng theo username hoặc email"
    )
    public ResponseEntity<ApiResponse<ListResponse<UserSimpleResponseDto>>> searchFriend(
            @RequestParam String keyword
    ) {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        List<UserSimpleResponseDto> result = userService.searchFriend(user, keyword);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }
}