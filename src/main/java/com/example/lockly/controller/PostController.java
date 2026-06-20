package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.CreatePostRequestDto;
import com.example.lockly.domain.dto.response.PostResponseDto;
import com.example.lockly.service.Impl.PostServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping(ApiPath.API_V1 + "/posts")
@Tag(name = "Post Controller", description = "API quản lý bài viết (post + image MinIO)")
public class PostController {

    PostServiceImpl postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo bài viết", description = "Upload image + caption + userId để tạo post")
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @Parameter(description = "File image")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "ID user")
            @RequestParam("userId") String userId,

            @Parameter(description = "Nội dung bài viết")
            @RequestParam("caption") String caption
    ) throws Exception {
        CreatePostRequestDto request = new CreatePostRequestDto(file, userId, caption);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo bài viết thành công", postService.createPost(request)));
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
}