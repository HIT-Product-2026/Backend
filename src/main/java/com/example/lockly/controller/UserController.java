package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.auth.LogoutRequestDto;
import com.example.lockly.domain.dto.request.auth.ResetPasswordRequestDto;
import com.example.lockly.domain.dto.response.common.ConversationResponseDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ConversationService;
import com.example.lockly.service.LocationService;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_V1 + "/user/me")
@Tag(name = "User Controller", description = "API quản lý user và friend system")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final LocationService locationService;
    private final RedisTemplate<String, Object> redisTemplate;


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

        // Kiểm tra tài khoản có bị đăng nhập từ nơi khác không
        boolean isUpdateFromUknownLocation = locationService.isUnknownLocation(longitude, latitude);
        if (isUpdateFromUknownLocation) {
            String jwtId = (String) redisTemplate.opsForValue().get(
                    "user_token:" + user.getId()
            );
            // logout
            authService.logout(LogoutRequestDto.from(jwtId));

            // Tự động thay password, buộc người dùng phải đổi lại password
            String email = user.getEmail();
            String password = UUID.randomUUID().toString();
            authService.resetPassword(
                    new ResetPasswordRequestDto(
                            email,
                            password
                    )
            );
        }

        // Giảm tần suất cập nhật vị trí
        boolean isDropRequest = locationService.isDropRequest(longitude, latitude);
        if (!isDropRequest)
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

    @PutMapping("/fcm-token")
    @Operation(summary = "Cập nhật fcm token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @RequestParam("fcm_token") String fcmToken
    ){
        User user = authService.getCurrentUser();
        userService.updateFcmTokenByUserId(user.getId(), fcmToken);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công"));
    }
}