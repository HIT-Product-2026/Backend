package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.UserBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_NOW + "/user/blocks")
@Tag(name = "User Block Controller", description = "API chặn và danh sách chặn")
public class UserBlockController {

    private final AuthService authService;
    private final UserBlockService userBlockService;

    @PostMapping("/{user_id}")
    @Operation(summary = "Chặn user")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @Parameter(description = "ID user muốn chặn")
            @PathVariable("user_id") UUID userId
    ) {
        User user = authService.getUserFromCache();
        userBlockService.blockUser(user, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Chặn user thành công"));
    }

    @DeleteMapping("/{user_id}")
    @Operation(summary = "Bỏ chặn user")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @Parameter(description = "ID user muốn bỏ chặn")
            @PathVariable("user_id") UUID userId
    ) {
        User user = authService.getUserFromCache();
        userBlockService.unblockUser(user, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Bỏ chặn user thành công"));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách user đã chặn")
    public ResponseEntity<ApiResponse<ListResponse<UserSimpleResponseDto>>> getBlockedUsers() {
        User user = authService.getUserFromCache();
        List<UserSimpleResponseDto> blockedUsers = userBlockService.getBlockedUsers(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", ListResponse.of(blockedUsers)));
    }
}