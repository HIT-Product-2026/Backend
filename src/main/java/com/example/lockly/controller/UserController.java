package com.example.lockly.controller;

<<<<<<< HEAD
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
=======
import com.example.lockly.common.ApiResponse;
import com.example.lockly.constant.SuccessMessage;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
<<<<<<< HEAD
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
=======
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

@Validated
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
<<<<<<< HEAD
@RequestMapping(ApiPath.API_V1 + "/user")
@Tag(name = "User Controller", description = "API quản lý user và friend system")
=======
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "Quản lý thông tin người dùng")
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
public class UserController {

    UserService userService;

<<<<<<< HEAD
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
            @RequestBody FriendshipsRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thành công" ,userService.sendFriendshipRequest(request)));
    }

    @PostMapping("/friend-request/accept")
    @Operation(summary = "Chấp nhận lời mời kết bạn", description = "Chuyển trạng thái PENDING → ACCEPTED")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> acceptFriendRequest(
            @RequestBody FriendshipsRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", userService.acceptAddFriendRequest(request)));
    }

    @PostMapping("/friend-request/reject")
    @Operation(summary = "Từ chối lời mời kết bạn", description = "Chuyển trạng thái REJECTED và xóa khỏi db")
    public ResponseEntity<ApiResponse<FriendshipsResponseDto>> rejectFriendRequest(
            @RequestBody FriendshipsRequestDto request
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
=======
    @GetMapping("/me")
    @Operation(
            summary = "Lấy thông tin cá nhân",
            description = "Trả về username và displayName của người dùng đang đăng nhập",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponseDto data = userService.getMyInfo(userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.User.GET_MY_INFO_SUCCESS, data));
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
    }
}