package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.common.response.ListResponse;
import com.example.lockly.common.util.CursorUtil;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.DetectNsfwPostRequestDto;
import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.dto.request.GetEmojiPostsRequestDto;
import com.example.lockly.domain.dto.request.Cursor;
import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.create.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostDetailResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(ApiPath.API_NOW + "/post")
@Tag(name = "Post Controller", description = "API quản lý bài viết (post + image MinIO)")
public class PostController {

    private final PostService postService;
    private final AuthService authService;
    private final RabbitMQService rabbitMQService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo bài viết", description = "Upload image + caption + userId, kinh độ, vĩ độ để tạo post")
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @Parameter(description = "File image")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Kinh độ")
            @RequestParam("longitude") Double longitude,

            @Parameter(description = "Vĩ độ")
            @RequestParam("latitude") Double latitude,

            @Parameter(description = "Nội dung bài viết")
            @RequestParam(value = "caption", required = false) String caption

    ) {

        CreatePostRequestDto request = new CreatePostRequestDto(
                file,
                caption,
                latitude,
                longitude
        );

        log.debug("Chuẩn bị tạo bài viết");
        PostResponseDto response = postService.createPost(request);

        log.debug("Tạo bài viết thành công");
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();


        // Gửi thông báo (đẩy vào queue)
        rabbitMQService.sendFcmNotification(
                user,
                response.id()
        );

        log.debug("Thông báo fcm thành công");

        // Đẩy vào queue (Client cần mở cổng sse để nhận response)
        rabbitMQService.detectNsfw(DetectNsfwPostRequestDto.from(response));

        log.debug("Detect thành công");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo bài viết thành công", response));
    }

    @GetMapping("/{post_id}")
    @Operation(summary = "Lấy bài viết theo ID", description = "Trả về thông tin post")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPostById(
            @Parameter(description = "ID bài viết")
            @PathVariable("post_id") UUID postId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", postService.getPostById(postId)));
    }

    @GetMapping("/{post_id}/image")
    @Operation(summary = "Lấy ảnh bài viết", description = "Trả về image binary từ MinIO")
    public ResponseEntity<ApiResponse<String>> getImage(
            @Parameter(description = "ID bài viết")
            @PathVariable("post_id") UUID postId
    ) throws Exception {

        String presignedUrl = postService.getPostImage(postId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", presignedUrl));
    }

    @PatchMapping("/{post_id}/mode")
    @Operation(summary = "Thay đổi mode location", description = "Quyết định có chia sẻ vị trí của bài post không")
    public ResponseEntity<ApiResponse<Void>> updateModeLocationPost(
            @Parameter(description = "ID bài post")
            @PathVariable("post_id") UUID postId,

            @Parameter(description = "Mode muốn đổi")
            @RequestParam("mode_location") PostModeLocation modeLocation
    ){
        postService.updateModeLocationPostById(postId, modeLocation);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công"));
    }

    @GetMapping("/{post_id}/location")
    @Operation(summary = "Lấy location của post", description = "Post để mode public mới có thể lấy")
    public ResponseEntity<ApiResponse<LocationPostResponseDto>> getLocationPost(
            @Parameter(description = "ID bài post")
            @PathVariable("post_id") UUID postId
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công", postService.getLocationPost(postId)));
    }

    @PostMapping("/emoji")
    @Operation(summary = "Thả càm xúc", description = "Người dùng thả cảm xúc vào bài viết")
    public ResponseEntity<ApiResponse<Void>> sendEmojiToPost(
            @Parameter(description = "ID bài post và loại cảm xúc được thả")
            @Valid @RequestBody ReactEmojiToPostRequestDto request
    ){
        postService.sendEmoji(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Thành công"));
    }

    @PostMapping("/emojis")
    @Operation(
            summary = "Lấy danh sách emoji của nhiều bài viết",
            description = "Truyền vào danh sách postId và trả về toàn bộ emoji của các bài viết"
    )
    public ResponseEntity<ApiResponse<ListResponse<EmojiPostResponseDto>>> getEmojiPosts(
            @Valid @RequestBody GetEmojiPostsRequestDto request
    ) {
        List<EmojiPostResponseDto> emojis =
                postService.getEmojiPosts(request.postIds());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Thành công",
                        ListResponse.of(emojis)
                ));
    }

    @GetMapping
    @Operation(
            summary = "Lấy danh sách bài viết của bạn bè và user của user (reels)",
            description = "Trả về list post của user"
    )
    public ResponseEntity<ApiResponse<ListResponse<PostDetailResponseDto>>> getPosts(
            @Parameter(description = "Thẻ đánh dấu trang")
            @RequestParam(name = "cursor", required = false) String cursor
    ) {
        // Lấy user từ cache thay vì db
        User user = authService.getUserFromCache();

        log.debug("curor: " + cursor);

        List<PostDetailResponseDto> listPost = postService.getFriendPosts(user, cursor);

        String nextCursor = null;
        if (!listPost.isEmpty()) {
            nextCursor = CursorUtil.encode(
                    Cursor.from(listPost.get(listPost.size() - 1))
            );
        }

        log.debug("Decode sucessful");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Thành công",
                        ListResponse.of(listPost, nextCursor)
                )
        );
    }
}