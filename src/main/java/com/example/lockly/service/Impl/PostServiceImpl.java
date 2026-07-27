package com.example.lockly.service.Impl;

import com.example.lockly.common.util.CursorUtil;
import com.example.lockly.common.util.FileUtil;
import com.example.lockly.config.MinioProperties;
import com.example.lockly.domain.dto.request.Cursor;
import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.create.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostDetailResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.domain.entity.main.EmojiPost;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.ForbiddenException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.repository.main.EmojiPostRepository;
import com.example.lockly.repository.main.PostsRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.*;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final MinioProperties props;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final ConversationRepository conversationRepository;
    private final AuthService authService;
    private final EmojiPostRepository emojiPostRepository;
    private final ProfileService profileService;
    private final MinIOService minIOService;
    private final String prefix = "post";
    private final int pageSize = 10;

    @Override
    @Transactional
    public PostResponseDto createPost(CreatePostRequestDto request) {

        if (request.file() == null || request.file().isEmpty())
            throw new BadRequestException("File is empty");

        User user = authService.getCurrentUser();

        log.debug("Lấy user thành công");

        MultipartFile file = request.file();
        String caption = request.caption();

        UUID postId = UuidCreator.getTimeOrderedEpoch();
        String objectName = FileUtil.getObjectNameFile(prefix, postId);

        log.debug("Chuẩn bị file thành công");

        // Lưu ảnh vào minIO
        minIOService.saveFile(file, objectName);

        String urlImage = minIOService.generatePresignedUrl(objectName);

        log.debug("Lưu ảnh thành công");

        // Set mode cho bài post
        PostModeLocation mode = (user.getMode() == UserMode.PRIVATE)
                ? PostModeLocation.PRIVATE
                : PostModeLocation.PUBLIC;

        log.debug("Set mode thành công");

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

        // Thêm point cho post (để tiện cho query với PortgreGIS)
        GeometryFactory geometryFactory =
                new GeometryFactory(new PrecisionModel(), 4326);


        Point point = geometryFactory.createPoint(
                new Coordinate(request.longitude(), request.latitude())
        );

        post.setLocation(point);

        log.debug("Tạo post thành công");

        postsRepository.save(post);

        log.debug("Lưu post thành công");

        // Cập nhật tiến trình nhiệm vụ
        profileService.updateProcessProfile(postId, objectName);

        log.debug("Tiến trình cập nhật thành công");

        if (mode == PostModeLocation.PRIVATE)
            return PostResponseDto.from(
                    post,
                    minIOService.generatePresignedUrl(post.getObjectName()),
                    null,
                    null
            );

        return PostResponseDto.from(post, urlImage);
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

        String objectName = post.getObjectName();

        String urlImage = minIOService.generatePresignedUrl(objectName);

        // Public mới trả tọa độ, không thì null
        if (post.getModeLocation() == PostModeLocation.PRIVATE){
            return PostResponseDto.from(post, urlImage, null, null);
        }
        return PostResponseDto.from(post, urlImage);
    }

    @Override
    public List<PostDetailResponseDto> getPostByUserId(UUID userId, String cursor) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "id", userId));

        Slice<Post> posts;

        if (cursor == null || cursor.isBlank()) {
            posts = postsRepository.findFirstPage(
                    user.getId(),
                    FriendshipStatus.ACCEPTED,
                    PageRequest.of(0, pageSize)
            );
        } else {
            Cursor cursorDecode = CursorUtil.decode(cursor);

            posts = postsRepository.findNextPage(
                    user.getId(),
                    FriendshipStatus.ACCEPTED,
                    cursorDecode.createdAt(),
                    cursorDecode.id(),
                    PageRequest.of(0, pageSize)
            );
        }

        // Danh sách post
        List<Post> listPost = posts.getContent();

        // Danh sách userId của chủ post
        List<UUID> userIds = listPost.stream()
                .map(Post::getUser)
                .map(User::getId)
                .distinct()
                .toList();

        // Conversation giữa user hiện tại và các chủ post
        List<Conversation> conversationList =
                conversationRepository.findByUserIdAndUserIds(user.getId(), userIds);

        // Map<userId, Conversation>
        Map<UUID, Conversation> conversationMap = conversationList.stream()
                .collect(Collectors.toMap(
                        c -> c.getUser1().getId().equals(user.getId())
                                ? c.getUser2().getId()
                                : c.getUser1().getId(),
                        Function.identity()
                ));

        return listPost.stream()
                .map(post -> PostDetailResponseDto.from(
                        post,
                        conversationMap.get(post.getUser().getId()),
                        minIOService.generatePresignedUrl(post.getObjectName())
                ))
                .toList();
    }

    @Override
    public List<PostDetailResponseDto> getFriendPosts(User user, String cursor) {

        Slice<Post> posts;

        if (cursor == null || cursor.isBlank()) {
            posts = postsRepository.findFriendPostsFirstPage(
                    user.getId(),
                    FriendshipStatus.ACCEPTED,
                    PageRequest.of(0, pageSize)
            );
        } else {
            Cursor cursorDecode = CursorUtil.decode(cursor);

            posts = postsRepository.findFriendPostsNextPage(
                    user.getId(),
                    FriendshipStatus.ACCEPTED,
                    cursorDecode.createdAt(),
                    cursorDecode.id(),
                    PageRequest.of(0, pageSize)
            );
        }

        // Lấy ds post
        List<Post> listPost = posts.getContent();

        // Lấy ds user của post
        List<UUID> userIds = listPost.stream()
                .map(Post::getUser)
                .map(User::getId)
                .distinct()
                .toList();

        // Lấy ds conversation của user
        List<Conversation> conversationList =
                conversationRepository.findByUserIdAndUserIds(user.getId(), userIds);

        // Tạo Map<userId, Conversation>
        Map<UUID, Conversation> conversationMap = conversationList.stream()
                .collect(Collectors.toMap(
                        c -> c.getUser1().getId().equals(user.getId())
                                ? c.getUser2().getId()
                                : c.getUser1().getId(),
                        Function.identity()
                ));

        return listPost.stream()
                .map(post -> PostDetailResponseDto.from(
                        post,
                        conversationMap.get(post.getUser().getId()),
                        minIOService.generatePresignedUrl(post.getObjectName())
                ))
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

        postsRepository.save(post);
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

    // UNIQUE(post_id, sender_id)
    @Override
    @Transactional
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
    @Transactional
    public void dropEmoji(ReactEmojiToPostRequestDto request) {

        Post post = postsRepository
                .findById(request.postId())
                .orElseThrow(() -> new BadRequestException("Post id", request.postId()));

        User user = authService.getCurrentUser();
        User postAuthor = post.getUser();

        if (!userService.isFriendByUserId(user.getId(), postAuthor.getId()))
            throw new ForbiddenException("User và chủ post không phải bạn bè");

        EmojiPost emojiPost = emojiPostRepository
                .findByPostIdAndSenderId(post.getId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EmojiPost",
                        "postId & senderId",
                        post.getId() + " & " + user.getId()
                ));

        emojiPostRepository.delete(emojiPost);
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