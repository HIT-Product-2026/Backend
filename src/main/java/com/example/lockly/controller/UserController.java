package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.FriendshipsRequestDto;
import com.example.lockly.domain.dto.response.ConversationResponseDto;
import com.example.lockly.domain.dto.response.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.PostResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ConversationService;
import com.example.lockly.service.PostService;
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
@RequestMapping(ApiPath.API_V1 + "/users/me")
@Tag(name = "User Controller", description = "API quản lý user và friend system")
public class UserController {

    UserService userService;
    ConversationService conversationService;
    PostService postService;
    AuthService authService;

    @GetMapping("/friends")
    @Operation(summary = "Lấy danh sách bạn bè", description = "Trả về danh sách bạn bè của user theo user_id"
    )
    public ResponseEntity<ApiResponse<ListResponse<UserResponseDto>>> getListFriendsByUserId(
    ) {
        User user = authService.getCurrentUser();

        List<UserResponseDto> listFriend =
                userService.findFriendByUserId(user.getId());

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

        userService.updateAvatar(file);

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

        userService.updateUserLocation(latitude, longitude);

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

        boolean isOnline = userService.isUserOnline();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", isOnline));
    }

    @GetMapping("/{user_id}/conversations")
    @Operation(summary = "Lấy danh sách hội thoại của user")
    public ResponseEntity<ApiResponse<ListResponse<ConversationResponseDto>>> getConversationsByUser(
            @PathVariable("user_id") String userId
    ) {

        List<ConversationResponseDto> result = conversationService.findByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(result)));
    }

    @GetMapping("/{user_id}/posts")
    @Operation(summary = "Lấy danh sách bài viết theo user", description = "Trả về list post của user")
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getPostsByUser(
            @Parameter(description = "ID user")
            @PathVariable("user_id") String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", postService.getPostByUserId(userId)));
    }
}