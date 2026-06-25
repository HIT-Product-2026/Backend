package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.request.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.EmojiPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.PostResponseDto;
import com.example.lockly.domain.entity.*;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ForbiddenException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.EmojiPostRepository;
import com.example.lockly.repository.PostsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.PostService;
import com.example.lockly.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {

    MinioProperties props;
    UserService userService;
    PostsRepository postsRepository;
    AuthService authService;
    EmojiPostRepository emojiPostRepository;
    MinIOService minIOService;
    String prefix = "post";
    int pageSize = 10;

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequestDto request) throws Exception {

        if (request.file() == null || request.file().isEmpty())
            throw new BadRequestException("File is empty");

        User user = authService.getCurrentUser();

        MultipartFile file = request.file();
        String caption = request.caption();

        String postId = UUID.randomUUID().toString();
        String objectName = FileUtil.getObjectNameFile(prefix, postId, file);

        try {
            minIOService.saveFile(file, objectName);

            // Set mode cho bài post
            PostModeLocation mode = (user.getMode() == UserMode.PRIVATE)
                    ? PostModeLocation.PRIVATE
                    : PostModeLocation.PUBLIC;

            Post post = Post.builder()
                    .id(postId)
                    .user(user)
                    .bucket(props.getBucketName())
                    .objectName(objectName)
                    .caption(caption)
                    .contentType(file.getContentType())
                    .longitude(request.longitude())
                    .latitude(request.latitude())
                    .modeLocation(mode)
                    .build();

            postsRepository.save(post);

            if (mode == PostModeLocation.PRIVATE)
                return PostResponseDto.from(post, FileUtil.getImageUrlApi(prefix, post.getId()), null, null);

            return PostResponseDto.from(post, FileUtil.getImageUrlApi(prefix, post.getId()));
        } catch (Exception e){
            minIOService.deleteFile(objectName);
            throw e;
        }
    }

    @Override
    public InputStream getPostImage(String postId) throws Exception {

        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        return minIOService.getFile(post.getObjectName());
    }

    @Override
    public PostResponseDto getPostById(String postId){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        String imageUrl = FileUtil.getImageUrlApi(prefix, postId);

        // Public mới trả tọa độ, không thì null
        if (post.getModeLocation() == PostModeLocation.PRIVATE){
            return PostResponseDto.from(post, imageUrl, null, null);
        }
        return PostResponseDto.from(post, imageUrl);
    }

    @Override
    public List<PostResponseDto> getPostByUserId(User user, int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<PostResponseDto> response = postsRepository
                .findByUserOrderByCreatedAtDesc(user, pageable)
                .stream()
                .map(post -> PostResponseDto
                        .from(post, FileUtil.getImageUrlApi(prefix, post.getId()))
                )
                .toList();

        return response;
    }

    @Override
    @Transactional
    public void updateModeLocationPostById(String postId, PostModeLocation modeLocation){
        User user = authService.getCurrentUser();

        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy post với id", postId));

        if (!post.getUser().getId().equals(user.getId()))
            throw new ForbiddenException("User id", user.getId());

        post.setModeLocation(modeLocation);
    }

    @Override
    public LocationPostResponseDto getLocationPost(String postId){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new BadRequestException("post_id", postId));

        if (post.getModeLocation() == PostModeLocation.PUBLIC){
            return new LocationPostResponseDto(post.getLatitude(), post.getLongitude());
        } else {
            return new LocationPostResponseDto(null, null);
        }
    }

    @Override
    @Transactional
    public void sendEmoji(EmojiPostRequestDto request){
        Post post = postsRepository
                .findById(request.postId())
                .orElseThrow(() -> new BadRequestException("Post id", request.postId()));

        User user = authService.getCurrentUser();
        User postAuthor = post.getUser();

        if (!userService.isFriendByUserId(user.getId(), postAuthor.getId()))
            throw new ForbiddenException("User và chủ post không phải bạn bè");

        EmojiPost emojiPost = EmojiPost.builder()
                .post(post)
                .sender(user)
                .emoji(request.emoji())
                .build();

        emojiPostRepository.save(emojiPost);
    }

}