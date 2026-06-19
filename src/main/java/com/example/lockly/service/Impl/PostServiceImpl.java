package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.constant.ApiPath;
import com.example.lockly.domain.dto.request.CreatePostRequestDto;
import com.example.lockly.domain.dto.response.PostResponseDto;
import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.User;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.PostsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.PostService;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
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

    MinioClient minioClient;
    MinioProperties props;
    UserRepository userRepository;
    PostsRepository postsRepository;
    String prefix = "posts";
    String prefixApi = "post";

    private String getImageUrl(Post post){
        return ApiPath.API_V1
                        + "/"
                        + prefixApi
                        + "/"
                        + post.getId()
                        + "/image";
    }

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequestDto request) throws Exception {

        if (request.file() == null || request.file().isEmpty())
            throw new BadRequestException("file", null);

        MultipartFile file = request.file();
        String userId = request.userId();
        String caption = request.caption();

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty())
            throw new IllegalArgumentException("Original filename is missing");

        String objectName = FileUtil.getObjectNameFile(prefix, userId, file);


        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(props.getBucketName())
                        .object(objectName)
                        .stream(
                                file.getInputStream(),
                                file.getSize(),
                                -1
                        )
                        .contentType(file.getContentType())
                        .build()
        );

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Post post = Post.builder()
                .user(user)
                .bucket(props.getBucketName())
                .objectName(objectName)
                .caption(caption)
                .contentType(file.getContentType())
                .build();

        postsRepository.save(post);
        String imageUrl = getImageUrl(post);

        PostResponseDto response = PostResponseDto.from(post, imageUrl);
        return response;
    }

    @Override
    public InputStream getPostImage(String postId) throws Exception {

        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(post.getBucket())
                        .object(post.getObjectName())
                        .build()
        );
    }

    @Override
    public PostResponseDto getPostById(String postId){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        return PostResponseDto.from(post, getImageUrl(post));
    }

    @Override
    public List<PostResponseDto> getPostByUserId(String userId){
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));


        List<PostResponseDto> response = postsRepository
                .findByUser(user)
                .stream()
                .map(post -> PostResponseDto
                        .from(post, getImageUrl(post))
                )
                .toList();

        return response;
    }
}