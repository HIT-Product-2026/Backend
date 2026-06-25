package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ConversationService;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

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
@RequestMapping(ApiPath.API_V1 + "/user/me")
@Tag(name = "User Controller", description = "API quản lý user và friend system")
public class UserController {

    UserService userService;
    ConversationService conversationService;
    AuthService authService;

    @GetMapping("/friends")
    @Operation(summary = "Lấy danh sách bạn bè", description = "Trả về danh sách bạn bè của user theo user_id"
    )
    public ResponseEntity<ApiResponse<ListResponse<UserResponseDto>>> getListFriendsByUserId(
    ) {
        User user = authService.getCurrentUser();
        List<UserResponseDto> listFriend = userService.findFriendsByUserId(user.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(listFriend)));
    }

    @GetMapping("/friendships")
    @Operation(summary = "Lấy danh sách lời mời kết bạn", description = "Trả về danh sách friend request (PENDING)")
    public ResponseEntity<ApiResponse<ListResponse<FriendshipsResponseDto>>> getFriendRequests(
    ) {
        User user = authService.getCurrentUser();

        List<FriendshipsResponseDto> result = userService.findFriendshipsByUserId(user.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }


    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật avatar", description = "Upload ảnh avatar lên MinIO và update user.avatarUrl")
    public ResponseEntity<ApiResponse<Void>> updateAvatar(
            @Parameter(description = "File ảnh avatar")
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        User user = authService.getCurrentUser();

        userService.updateAvatarByUserId(user.getId(), file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật avatar thành công", null));
    }

    @PostMapping("/location")
    @Operation(summary = "Cập nhật vị trí user", description = "Lưu latitude (vĩ độ) và longitude (kinh độ) của user")
    public ResponseEntity<ApiResponse<Void>> updateUserLocation(
            @RequestParam("latitude") Double latitude,

            @RequestParam("longitude") Double longitude
    ) {
        User user = authService.getCurrentUser();

        userService.updateUserLocationByUserId(user.getId(), latitude, longitude);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật vị trí thành công", null));
    }

    @GetMapping("/online")
    @Operation(summary = "Kiểm tra trạng thái online",
            description = "Trả về true nếu user hoạt động trong 5 phút gần nhất"
    )
    public ResponseEntity<ApiResponse<Boolean>> isUserOnline(
    ) {

        User user = authService.getCurrentUser();
        boolean isOnline = userService.isUserOnlineByUserId(user.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", isOnline));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Lấy danh sách hội thoại của user")
    public ResponseEntity<ApiResponse<ListResponse<ConversationResponseDto>>> getAllConversations(
    ) {

        List<ConversationResponseDto> result = conversationService.findAll();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }

    @PutMapping("/display-name")
    @Operation(
            summary = "Cập nhật display name",
            description = "Cập nhật tên hiển thị của user hiện tại"
    )
    public ResponseEntity<ApiResponse<Void>> updateDisplayName(
            @RequestParam("displayName") String displayName
    ) {
        User user = authService.getCurrentUser();
        userService.updateDisplayNameByUserId(user.getId(), displayName);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật display name thành công", null));
    }

    @PutMapping("/mode")
    @Operation(
            summary = "Cập nhật chế độ người dùng",
            description = "Cập nhật UserMode (ví dụ: PUBLIC / PRIVATE / etc)"
    )
    public ResponseEntity<ApiResponse<Void>> updateMode(
            @RequestParam("mode") UserMode mode
    ) {
        User user = authService.getCurrentUser();
        userService.updateModeByUserId(user.getId(), mode);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật mode thành công", null));
    }
}