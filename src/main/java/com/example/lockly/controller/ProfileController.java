package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.UpdateProfileRequestDto;
import com.example.lockly.domain.dto.request.create.CreateProfileRequestDto;
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

    @PutMapping("/{profileId}")
    @Operation(
            summary = "Cập nhật profile",
            description = "Cập nhật thông tin profile theo ID"
    )
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @Parameter(description = "ID của profile")
            @PathVariable("profileId") UUID profileId,

            @RequestBody @Valid UpdateProfileRequestDto request
    ) {

        profileService.updateProfile(profileId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Cập nhật profile thành công", null));
    }

//    @PostMapping(value = "/face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Operation(
//            summary = "Đăng ký khuôn mặt",
//            description = "Đăng ký khuôn mặt của user hiện tại với AI Service"
//    )
//    public ResponseEntity<ApiResponse<Void>> registerFace(
//            @Parameter(description = "Ảnh khuôn mặt")
//            @RequestParam("file") MultipartFile file
//    ) {
//
//        User user = authService.getCurrentUser();
//
//        profileService.registerFace(user.getId(), objectName);
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(ApiResponse.success("Đăng ký khuôn mặt thành công", null));
//    }
}
