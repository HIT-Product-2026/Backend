package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.request.CreatePostRequestDto;
import com.example.lockly.domain.dto.response.PostResponseDto;
import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.PostsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {

    MinioProperties props;
    UserRepository userRepository;
    PostsRepository postsRepository;
    MinIOService minIOService;
    String prefix = "post";

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequestDto request) throws Exception {

        if (request.file() == null || request.file().isEmpty())
            throw new BadRequestException("File is empty");

        MultipartFile file = request.file();
        String userId = request.userId();
        String caption = request.caption();

        String postId = UUID.randomUUID().toString();
        String objectName = FileUtil.getObjectNameFile(prefix, postId, file);

        try {
            minIOService.saveFile(file, objectName);

            User user = userRepository
                    .findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            Post post = Post.builder()
                    .id(postId)
                    .user(user)
                    .bucket(props.getBucketName())
                    .objectName(objectName)
                    .caption(caption)
                    .contentType(file.getContentType())
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

        return PostResponseDto.from(post, FileUtil.getImageUrlApi(prefix, postId));
    }

    @Override
    public List<PostResponseDto> getPostByUserId(String userId){
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));


        List<PostResponseDto> response = postsRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(post -> PostResponseDto
                        .from(post, FileUtil.getImageUrlApi(prefix, post.getId()))
                )
                .toList();

        return response;
    }
}