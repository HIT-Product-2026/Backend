package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping(ApiPath.API_V1 + "/user")
@Tag(name = "User Controller", description = "API quản lý user và friend system")
public class UserController {

    UserService userService;

    @GetMapping("/friends/{user_id}")
    @Operation(summary = "Lấy danh sách bạn bè", description = "Trả về danh sách bạn bè của user theo user_id"
    )
    public ResponseEntity<ApiResponse<ListResponse<UserResponseDto>>> getListFriendsByUserId(
            @Parameter(description = "ID của user")
            @PathVariable("user_id") String userId
    ) {
        List<UserResponseDto> listFriend =
                userService.findAllListFriendByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(listFriend)));
    }

    @GetMapping("/friendships/{user_id}")
    @Operation(summary = "Lấy danh sách lời mời kết bạn", description = "Trả về danh sách friend request (PENDING)")
    public ResponseEntity<ApiResponse<ListResponse<FriendshipsResponseDto>>> getFriendRequests(
            @Parameter(description = "ID của user")
            @PathVariable("user_id") String userId
    ) {
        List<FriendshipsResponseDto> result = userService.findAllFriendshipsByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }

    @PostMapping("/friend-request")
    @Operation(summary = "Gửi lời mời kết bạn", description = "Tạo friendship request trạng thái PENDING")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> sendFriendRequest(
            @RequestBody @Valid FriendshipsRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thành công" ,userService.sendFriendshipRequest(request)));
    }

    @PostMapping("/friend-request/accept")
    @Operation(summary = "Chấp nhận lời mời kết bạn", description = "Chuyển trạng thái PENDING → ACCEPTED")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> acceptFriendRequest(
            @RequestBody @Valid FriendshipsRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", userService.acceptAddFriendRequest(request)));
    }

    @PostMapping("/friend-request/reject")
    @Operation(summary = "Từ chối lời mời kết bạn", description = "Chuyển trạng thái REJECTED và xóa khỏi db")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> rejectFriendRequest(
            @RequestBody @Valid FriendshipsRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", userService.rejectAddFriendRequest(request)));
    }

    @PostMapping(value = "/{user_id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật avatar", description = "Upload ảnh avatar lên MinIO và update user.avatarUrl")
    public ResponseEntity<ApiResponse<Void>> updateAvatar(
            @Parameter(description = "ID user")
            @PathVariable("user_id") String userId,

            @Parameter(description = "File ảnh avatar")
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        userService.updateAvatar(userId, file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật avatar thành công", null));
    }

    @PostMapping("/{user_id}/location")
    @Operation(summary = "Cập nhật vị trí user", description = "Lưu latitude (vĩ độ) và longitude (kinh độ) của user")
    public ResponseEntity<ApiResponse<Void>> updateUserLocation(
            @Parameter(description = "ID user")
            @PathVariable("user_id") String userId,

            @RequestParam("latitude") Double latitude,

            @RequestParam("longitude") Double longitude
    ) {

        userService.updateUserLocation(userId, latitude, longitude);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật vị trí thành công", null));
    }

    @GetMapping("/{user_id}/online")
    @Operation(summary = "Kiểm tra trạng thái online",
            description = "Trả về true nếu user hoạt động trong 5 phút gần nhất"
    )
    public ResponseEntity<ApiResponse<Boolean>> isUserOnline(
            @Parameter(description = "ID user")
            @PathVariable("user_id") String userId
    ) {

        boolean isOnline = userService.isUserOnline(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", isOnline));
    }
}