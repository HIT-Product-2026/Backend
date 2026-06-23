package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.CreatePostRequestDto;
import com.example.lockly.domain.dto.response.FcmPostResponseDto;
import com.example.lockly.domain.dto.response.PostResponseDto;
import com.example.lockly.domain.dto.response.UserResponseDto;
import com.example.lockly.domain.entity.PostModeLocation;
import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.FcmService;
import com.example.lockly.service.Impl.PostServiceImpl;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping(ApiPath.API_V1 + "/post")
@Tag(name = "Post Controller", description = "API quản lý bài viết (post + image MinIO)")
public class PostController {

    PostServiceImpl postService;
    UserService userService;
    FcmService fcmService;
    AuthService authService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo bài viết", description = "Upload image + caption + userId, kinh độ, vĩ độ để tạo post")
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @Parameter(description = "File image")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Nội dung bài viết")
            @RequestParam("caption") String caption,

            @Parameter(description = "Kinh độ")
            @RequestParam("longitude") Double longitude,

            @Parameter(description = "Vĩ độ")
            @RequestParam("latitude") Double latitude

    ) throws Exception {

        CreatePostRequestDto request = new CreatePostRequestDto(
                file,
                caption,
                latitude,
                longitude
        );

        PostResponseDto post = postService.createPost(request);

        User user = authService.getCurrentUser();

        // Gửi thông báo
        List<String> fcmTokens = userService.findFcmTokenOfFriendsByUserId();
        FcmPostResponseDto data = fcmService.createFcmPostResponse(user.getId(), post.getId());
        fcmService.sendToManySilent(fcmTokens, data);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo bài viết thành công", post));
    }

    @GetMapping("/{post_id}")
    @Operation(summary = "Lấy bài viết theo ID", description = "Trả về thông tin post")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPost(
            @Parameter(description = "ID bài viết")
            @PathVariable("post_id") String postId
    ) {
        return ResponseEntity
                .ok(ApiResponse.success("Thành công", postService.getPostById(postId)));
    }

    @GetMapping("/{post_id}/image")
    @Operation(summary = "Lấy ảnh bài viết", description = "Trả về image binary từ MinIO")
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "ID bài viết")
            @PathVariable("post_id") String postId
    ) throws Exception {
        InputStream inputStream = postService.getPostImage(postId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(inputStream.readAllBytes());
    }

    @PatchMapping("/mode")
    @Operation(summary = "Thay đổi mode location", description = "Quyết định có chia sẻ vị trí của bài post không")
    public ResponseEntity<ApiResponse<Void>> updateModeLocationPost(
            @Parameter(description = "ID bài post")
            @RequestParam("post_id") String postId,

            @Parameter(description = "Mode muốn đổi")
            @RequestParam("mode_location") PostModeLocation modeLocation
    ){
        postService.updateModeLocationPostById(postId, modeLocation);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công"));
    }
}