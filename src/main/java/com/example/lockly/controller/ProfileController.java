package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.UpdateProfileRequestDto;
import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.API_V1 + "/profile")
@Tag(name = "Profile Controller", description = "API quản lý profile người dùng")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    @PutMapping()
    @Operation(
            summary = "Cập nhật profile",
            description = "Cập nhật thông tin profile theo ID"
    )
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @RequestBody @Valid UpdateProfileRequestDto request
    ) {
        
        profileService.updateProfile(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật profile thành công", null));
    }

    @PostMapping("/face/register")
    @Operation(
            summary = "Đăng ký khuôn mặt",
            description = "Đăng ký khuôn mặt của người dùng hiện tại bằng ảnh đã upload lên MinIO"
    )
    public ResponseEntity<ApiResponse<Void>> registerFace(
            @Parameter(description = "Object name của ảnh trên MinIO")
            @RequestParam("image") MultipartFile file
    ) {
        User user = authService.getCurrentUser();

        profileService.registerFace(user.getId(), file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Đăng ký khuôn mặt thành công", null));
    }

    @GetMapping("/face/check")
    @Operation(
            summary = "Kiểm tra đã đăng ký khuôn mặt",
            description = "Kiểm tra người dùng hiện tại đã đăng ký khuôn mặt hay chưa"
    )
    public ResponseEntity<ApiResponse<Boolean>> checkFace() {
        User user = authService.getCurrentUser();

        Boolean hasFace = profileService.checkFace(user.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Kiểm tra khuôn mặt thành công",
                        hasFace
                ));
    }
}
