package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.request.CreatePostRequestDto;
import com.example.lockly.domain.dto.response.PostResponseDto;
import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.PostModeLocation;
import com.example.lockly.domain.entity.User;
import com.example.lockly.domain.entity.UserMode;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ForbiddenException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.PostsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    UserRepository userRepository;
    PostsRepository postsRepository;
    AuthService authService;
    MinIOService minIOService;
    String prefix = "post";

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
    public List<PostResponseDto> getPostByUserId(){
        User user = authService.getCurrentUser();


        List<PostResponseDto> response = postsRepository
                .findByUserOrderByCreatedAtDesc(user)
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
}