package com.example.app.unit;

import com.example.lockly.config.minio.MinioProperties;
import com.example.lockly.domain.dto.request.Cursor;
import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.create.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostDetailResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.EmojiPost;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.Emoji;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.ForbiddenException;
import com.example.lockly.exception.nonRetryException.ResourceNotFoundException;
import com.example.lockly.repository.main.ConversationRepository;
import com.example.lockly.repository.main.EmojiPostRepository;
import com.example.lockly.repository.main.PostsRepository;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.LocationService;
import com.example.lockly.service.MinIOService;
import com.example.lockly.service.ProfileService;
import com.example.lockly.service.UserService;
import com.example.lockly.service.Impl.PostServiceImpl;
import com.example.lockly.common.util.CursorUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {
    @Mock
    private MinioProperties props;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostsRepository postsRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AuthService authService;

    @Mock
    private EmojiPostRepository emojiPostRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private MinIOService minIOService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private PostServiceImpl postService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(UUID.randomUUID())
                .username("user")
                .displayName("User")
                .email("user@test.com")
                .passwordHash("password")
                .mode(UserMode.PUBLIC)
                .build();

        post = Post.builder()
                .id(UUID.randomUUID())
                .user(user)
                .bucket("bucket")
                .objectName("post/test.jpg")
                .caption("caption")
                .latitude(21.02)
                .longitude(105.84)
                .modeLocation(PostModeLocation.PUBLIC)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createPost_ShouldThrow_WhenFileNull() {

        CreatePostRequestDto request =
                new CreatePostRequestDto(
                        null,
                        "caption",
                        21.0,
                        105.0
                );

        assertThrows(
                BadRequestException.class,
                () -> postService.createPost(request)
        );
    }

    @Test
    void createPost_ShouldThrow_WhenFileEmpty() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "",
                        "image/jpeg",
                        new byte[0]
                );

        CreatePostRequestDto request =
                new CreatePostRequestDto(
                        file,
                        "caption",
                        21.0,
                        105.0
                );

        assertThrows(
                BadRequestException.class,
                () -> postService.createPost(request)
        );
    }

    @Test
    void createPost_PublicMode_ShouldReturnLocation() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.jpg",
                        "image/jpeg",
                        "abc".getBytes()
                );

        CreatePostRequestDto request =
                new CreatePostRequestDto(
                        file,
                        "caption",
                        21.02,
                        105.84
                );

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(props.getBucketName())
                .thenReturn("bucket");

        when(minIOService.generatePresignedUrl(anyString()))
                .thenReturn("url");

        PostResponseDto response =
                postService.createPost(request);

        ArgumentCaptor<Post> captor =
                ArgumentCaptor.forClass(Post.class);

        verify(postsRepository).save(captor.capture());

        Post saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals(PostModeLocation.PUBLIC, saved.getModeLocation());
        assertEquals(21.02, saved.getLatitude());
        assertEquals(105.84, saved.getLongitude());

        assertNotNull(saved.getLocation());

        verify(minIOService)
                .saveFile(eq(file), anyString());

        verify(profileService)
                .updateProcessProfile(saved.getId(), saved.getObjectName());

        assertNotNull(response);
        assertEquals(21.02, response.latitude());
    }

    @Test
    void createPost_PrivateMode_ShouldHideLocation() {

        user.setMode(UserMode.PRIVATE);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.jpg",
                        "image/jpeg",
                        "abc".getBytes()
                );

        CreatePostRequestDto request =
                new CreatePostRequestDto(
                        file,
                        "caption",
                        21.0,
                        105.0
                );

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(props.getBucketName())
                .thenReturn("bucket");

        when(minIOService.generatePresignedUrl(anyString()))
                .thenReturn("url");

        PostResponseDto response =
                postService.createPost(request);

        assertNull(response.latitude());
        assertNull(response.longitude());

        ArgumentCaptor<Post> captor =
                ArgumentCaptor.forClass(Post.class);

        verify(postsRepository).save(captor.capture());

        assertEquals(
                PostModeLocation.PRIVATE,
                captor.getValue().getModeLocation()
        );
    }

    @Test
    void getPostImage_ShouldReturnUrl() {

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(minIOService.generatePresignedUrl(post.getObjectName()))
                .thenReturn("image-url");

        String result =
                postService.getPostImage(post.getId());

        assertEquals("image-url", result);
    }

    @Test
    void getPostImage_PostNotFound() {

        when(postsRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getPostImage(UUID.randomUUID())
        );
    }

    @Test
    void getPostById_Public() {

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(minIOService.generatePresignedUrl(post.getObjectName()))
                .thenReturn("url");

        PostResponseDto dto =
                postService.getPostById(post.getId());

        assertEquals(post.getLatitude(), dto.latitude());
        assertEquals(post.getLongitude(), dto.longitude());
    }

    @Test
    void getPostById_Private() {

        post.setModeLocation(PostModeLocation.PRIVATE);

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(minIOService.generatePresignedUrl(anyString()))
                .thenReturn("url");

        PostResponseDto dto =
                postService.getPostById(post.getId());

        assertNull(dto.latitude());
        assertNull(dto.longitude());
    }

    @Test
    void getPostById_NotFound() {

        when(postsRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getPostById(UUID.randomUUID())
        );
    }

    @Test
    void getPostByUserId_FirstPage() {

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        Slice<Post> slice =
                new SliceImpl<>(List.of(post));

        when(postsRepository.findFirstPage(
                eq(user.getId()),
                any(PageRequest.class)
        )).thenReturn(slice);

        when(conversationRepository.findByUserIdAndUserIds(
                eq(user.getId()),
                anyList()
        )).thenReturn(List.of());

        when(minIOService.generatePresignedUrl(post.getObjectName()))
                .thenReturn("url");

        when(locationService.getProvinceFullName(
                post.getLatitude(),
                post.getLongitude()
        )).thenReturn("Ha Noi");

        List<PostDetailResponseDto> result =
                postService.getPostByUserId(user.getId(), null);

        assertEquals(1, result.size());

        verify(postsRepository)
                .findFirstPage(eq(user.getId()), any(PageRequest.class));
    }

    @Test
    void getPostByUserId_NextPage() {

        Cursor cursor =
                new Cursor(
                        LocalDateTime.now().minusDays(1),
                        UUID.randomUUID()
                );

        String encoded =
                CursorUtil.encode(cursor);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        Slice<Post> slice =
                new SliceImpl<>(List.of(post));

        when(postsRepository.findNextPage(
                eq(user.getId()),
                eq(cursor.createdAt()),
                eq(cursor.id()),
                any(PageRequest.class)
        )).thenReturn(slice);

        when(conversationRepository.findByUserIdAndUserIds(
                any(),
                anyList()
        )).thenReturn(List.of());

        when(minIOService.generatePresignedUrl(anyString()))
                .thenReturn("url");

        when(locationService.getProvinceFullName(any(), any()))
                .thenReturn("Ha Noi");

        List<PostDetailResponseDto> result =
                postService.getPostByUserId(
                        user.getId(),
                        encoded
                );

        assertEquals(1, result.size());

        verify(postsRepository)
                .findNextPage(
                        eq(user.getId()),
                        eq(cursor.createdAt()),
                        eq(cursor.id()),
                        any(PageRequest.class)
                );
    }

    @Test
    void getPostByUserId_UserNotFound() {

        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.getPostByUserId(
                        UUID.randomUUID(),
                        null
                )
        );
    }

    @Test
    void getFriendPosts_FirstPage() {

        Slice<Post> slice =
                new SliceImpl<>(List.of(post));

        when(postsRepository.findFriendPostsFirstPage(
                eq(user.getId()),
                eq(FriendshipStatus.ACCEPTED),
                any(PageRequest.class)
        )).thenReturn(slice);

        when(conversationRepository.findByUserIdAndUserIds(
                any(),
                anyList()
        )).thenReturn(List.of());

        when(minIOService.generatePresignedUrl(anyString()))
                .thenReturn("url");

        when(locationService.getProvinceFullName(any(), any()))
                .thenReturn("Ha Noi");

        List<PostDetailResponseDto> result =
                postService.getFriendPosts(user, null);

        assertEquals(1, result.size());
    }

    @Test
    void getFriendPosts_NextPage() {

        Cursor cursor =
                new Cursor(
                        LocalDateTime.now(),
                        UUID.randomUUID()
                );

        String encoded =
                CursorUtil.encode(cursor);

        Slice<Post> slice =
                new SliceImpl<>(List.of(post));

        when(postsRepository.findFriendPostsNextPage(
                eq(user.getId()),
                eq(FriendshipStatus.ACCEPTED),
                eq(cursor.createdAt()),
                eq(cursor.id()),
                any(PageRequest.class)
        )).thenReturn(slice);

        when(conversationRepository.findByUserIdAndUserIds(
                any(),
                anyList()
        )).thenReturn(List.of());

        when(minIOService.generatePresignedUrl(anyString()))
                .thenReturn("url");

        when(locationService.getProvinceFullName(any(), any()))
                .thenReturn("Ha Noi");

        List<PostDetailResponseDto> result =
                postService.getFriendPosts(user, encoded);

        assertEquals(1, result.size());
    }

    @Test
    void updateModeLocationPostById_ShouldUpdate() {

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        postService.updateModeLocationPostById(
                post.getId(),
                PostModeLocation.PRIVATE
        );

        assertEquals(
                PostModeLocation.PRIVATE,
                post.getModeLocation()
        );

        verify(postsRepository).save(post);
    }

    @Test
    void updateModeLocationPostById_Forbidden() {

        User other =
                User.builder()
                        .id(UUID.randomUUID())
                        .build();

        post.setUser(other);

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        assertThrows(
                ForbiddenException.class,
                () -> postService.updateModeLocationPostById(
                        post.getId(),
                        PostModeLocation.PRIVATE
                )
        );
    }

    @Test
    void updateModeLocationPostById_PostNotFound() {

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(postsRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> postService.updateModeLocationPostById(
                        UUID.randomUUID(),
                        PostModeLocation.PUBLIC
                )
        );
    }

    @Test
    void getLocationPost_Public() {

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        LocationPostResponseDto dto =
                postService.getLocationPost(post.getId());

        assertEquals(
                post.getLatitude(),
                dto.latitude()
        );

        assertEquals(
                post.getLongitude(),
                dto.longitude()
        );
    }

    @Test
    void getLocationPost_Private() {

        post.setModeLocation(PostModeLocation.PRIVATE);

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        LocationPostResponseDto dto =
                postService.getLocationPost(post.getId());

        assertNull(dto.latitude());
        assertNull(dto.longitude());
    }

    @Test
    void getLocationPost_PostNotFound() {

        when(postsRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> postService.getLocationPost(
                        UUID.randomUUID()
                )
        );
    }

    @Test
    void sendEmoji_ShouldSave() {

        ReactEmojiToPostRequestDto request =
                new ReactEmojiToPostRequestDto(
                        post.getId(),
                        Emoji.LIKE
                );

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(userService.isFriendByUserId(
                user,
                post.getUser().getId()
        )).thenReturn(true);

        postService.sendEmoji(request);

        ArgumentCaptor<EmojiPost> captor =
                ArgumentCaptor.forClass(EmojiPost.class);

        verify(emojiPostRepository).save(captor.capture());

        EmojiPost saved = captor.getValue();

        assertEquals(post, saved.getPost());
        assertEquals(user, saved.getSender());
        assertEquals(Emoji.LIKE, saved.getEmoji());
    }

    @Test
    void sendEmoji_PostNotFound() {

        ReactEmojiToPostRequestDto request =
                new ReactEmojiToPostRequestDto(
                        UUID.randomUUID(),
                        Emoji.LIKE
                );

        when(postsRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> postService.sendEmoji(request)
        );
    }

    @Test
    void sendEmoji_NotFriend() {

        ReactEmojiToPostRequestDto request =
                new ReactEmojiToPostRequestDto(
                        post.getId(),
                        Emoji.LIKE
                );

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(userService.isFriendByUserId(
                user,
                post.getUser().getId()
        )).thenReturn(false);

        assertThrows(
                ForbiddenException.class,
                () -> postService.sendEmoji(request)
        );

        verify(emojiPostRepository, never()).save(any());
    }

    @Test
    void dropEmoji_ShouldDelete() {

        EmojiPost emojiPost =
                EmojiPost.builder()
                        .post(post)
                        .sender(user)
                        .emoji(Emoji.LIKE)
                        .build();

        ReactEmojiToPostRequestDto request =
                new ReactEmojiToPostRequestDto(
                        post.getId(),
                        Emoji.LIKE
                );

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(userService.isFriendByUserId(
                user,
                post.getUser().getId()
        )).thenReturn(true);

        when(emojiPostRepository.findByPostIdAndSenderId(
                post.getId(),
                user.getId()
        )).thenReturn(Optional.of(emojiPost));

        postService.dropEmoji(request);

        verify(emojiPostRepository).delete(emojiPost);
    }

    @Test
    void dropEmoji_PostNotFound() {

        ReactEmojiToPostRequestDto request =
                new ReactEmojiToPostRequestDto(
                        UUID.randomUUID(),
                        Emoji.LIKE
                );

        when(postsRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> postService.dropEmoji(request)
        );
    }

    @Test
    void dropEmoji_NotFriend() {

        ReactEmojiToPostRequestDto request =
                new ReactEmojiToPostRequestDto(
                        post.getId(),
                        Emoji.LIKE
                );

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(userService.isFriendByUserId(
                user,
                post.getUser().getId()
        )).thenReturn(false);

        assertThrows(
                ForbiddenException.class,
                () -> postService.dropEmoji(request)
        );

        verify(emojiPostRepository, never()).delete(any());
    }

    @Test
    void dropEmoji_EmojiNotFound() {

        ReactEmojiToPostRequestDto request =
                new ReactEmojiToPostRequestDto(
                        post.getId(),
                        Emoji.LIKE
                );

        when(postsRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(authService.getUserFromCache())
                .thenReturn(user);

        when(userService.isFriendByUserId(
                user,
                post.getUser().getId()
        )).thenReturn(true);

        when(emojiPostRepository.findByPostIdAndSenderId(
                post.getId(),
                user.getId()
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> postService.dropEmoji(request)
        );
    }

    @Test
    void getEmojiPosts_ShouldReturnList() {

        EmojiPost emojiPost =
                EmojiPost.builder()
                        .id(UUID.randomUUID())
                        .post(post)
                        .sender(user)
                        .emoji(Emoji.LIKE)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(emojiPostRepository.findByPostIdsWithSender(
                List.of(post.getId())
        )).thenReturn(List.of(emojiPost));

        List<EmojiPostResponseDto> result =
                postService.getEmojiPosts(
                        List.of(post.getId())
                );

        assertEquals(1, result.size());

        assertEquals(
                Emoji.LIKE,
                result.get(0).emoji()
        );

        assertEquals(
                post.getId(),
                result.get(0).postId()
        );
    }
}
