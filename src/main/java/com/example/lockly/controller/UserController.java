package com.example.lockly.controller;

import com.example.lockly.common.ApiResponse;
import com.example.lockly.constant.SuccessMessage;
import com.example.lockly.domain.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "Quản lý thông tin người dùng")
public class UserController {

    UserService customUserDetailsService;

    @GetMapping("/me")
    @Operation(
            summary = "Lấy thông tin cá nhân",
            description = "Trả về username và displayName của người dùng đang đăng nhập",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponseDto data = customUserDetailsService.getMyInfo(userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessMessage.User.GET_MY_INFO_SUCCESS, data));
    }
}