package com.example.lockly.service.Impl;

import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.*;
import com.example.lockly.exception.BadRequestException;
import com.example.lockly.exception.ForbiddenException;
import com.example.lockly.exception.ResourceNotFoundException;
import com.example.lockly.repository.EmojiPostRepository;
import com.example.lockly.repository.FriendshipsRepository;
import com.example.lockly.repository.PostsRepository;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.PostService;
import com.example.lockly.service.UserService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
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
public class PostServiceImpl implements PostService {

    private final MinioProperties props;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final AuthService authService;
    private final EmojiPostRepository emojiPostRepository;
    private final FriendshipsRepository friendshipsRepository;
    private final MinIOService minIOService;
    private final String prefix = "post";
    private final int pageSize = 10;

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequestDto request) throws Exception {

        if (request.file() == null || request.file().isEmpty())
            throw new BadRequestException("File is empty");

        User user = authService.getCurrentUser();

        MultipartFile file = request.file();
        String caption = request.caption();

        UUID postId = UuidCreator.getTimeOrderedEpoch();
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
                    .longitude(request.longitude())
                    .latitude(request.latitude())
                    .modeLocation(mode)
                    .build();

            postsRepository.save(post);

            if (mode == PostModeLocation.PRIVATE)
                return PostResponseDto.from(post, FileUtil.getImageUrlApi(prefix, post.getId()), null, null);

            return PostResponseDto.from(post);
        } catch (Exception e){
            minIOService.deleteFile(objectName);
            throw e;
        }
    }

    @Override
    public InputStream getPostImage(UUID postId) throws Exception {

        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        return minIOService.getFile(post.getObjectName());
    }

    @Override
    public PostResponseDto getPostById(UUID postId){
        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        String imageUrl = FileUtil.getImageUrlApi(prefix, postId);

        // Public mới trả tọa độ, không thì null
        if (post.getModeLocation() == PostModeLocation.PRIVATE){
            return PostResponseDto.from(post, imageUrl, null, null);
        }
        return PostResponseDto.from(post);
    }

    @Override
    public List<PostResponseDto> getPostByUserId(UUID userId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return postsRepository
                .findByUserOrderByCreatedAtDesc(user, pageable)
                .stream()
                .map(PostResponseDto::from)
                .toList();
    }

    @Override
    public List<PostResponseDto> getFriendPosts(UUID userId, int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<UUID> friendIds = friendshipsRepository.findFriendIds(userId);

        return postsRepository
                .findByUserIdInOrderByCreatedAtDesc(friendIds, pageable)
                .stream()
                .map(PostResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public void updateModeLocationPostById(UUID postId, PostModeLocation modeLocation){
        User user = authService.getCurrentUser();

        Post post = postsRepository
                .findById(postId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy post với id", postId));

        if (!post.getUser().getId().equals(user.getId()))
            throw new ForbiddenException("User id", user.getId());

        post.setModeLocation(modeLocation);
    }

    @Override
    public LocationPostResponseDto getLocationPost(UUID postId){
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
    public void sendEmoji(ReactEmojiToPostRequestDto request){
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

    @Override
    public List<EmojiPostResponseDto> getEmojiPosts(List<UUID> postIds) {
        return emojiPostRepository
                .findByPostIdsWithSender(postIds)
                .stream()
                .map(EmojiPostResponseDto::from)
                .toList();
    }
}