package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreatePostRequestDto;
import com.example.lockly.domain.dto.request.create.ReactEmojiToPostRequestDto;
import com.example.lockly.domain.dto.response.LocationPostResponseDto;
import com.example.lockly.domain.dto.response.common.EmojiPostResponseDto;
import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.exception.nonRetryException.NotFoundException;
import com.example.lockly.repository.main.PostRepository;
import com.example.lockly.repository.main.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PostService Unit Tests")
class PostServiceTest {

    @Autowired
    private PostService postService;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MinIOService minIOService;

    @MockBean
    private QdrantService qdrantService;

    @MockBean
    private RabbitMQService rabbitMQService;

    private User testUser;
    private Post testPost;
    private UUID testUserId;
    private UUID testPostId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPostId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        testPost = Post.builder()
                .id(testPostId)
                .user(testUser)
                .caption("Test post caption")
                .imageUrl("https://example.com/image.jpg")
                .latitude(10.7769)
                .longitude(106.7009)
                .createdAt(LocalDateTime.now())
                .modeLocation(PostModeLocation.PUBLIC)
                .build();
    }

    @Test
    @DisplayName("Should create post successfully")
    void testCreatePost_Success() {
        // Arrange
        CreatePostRequestDto request = new CreatePostRequestDto();
        request.setCaption("New post");
        request.setLatitude(10.7769);
        request.setLongitude(106.7009);
        request.setModeLocation(PostModeLocation.PUBLIC);

        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        PostResponseDto response = postService.createPost(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test post caption", response.getCaption());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should get post by ID")
    void testGetPostById_Success() {
        // Arrange
        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));

        // Act
        PostResponseDto response = postService.getPostById(testPostId);

        // Assert
        assertNotNull(response);
        assertEquals(testPost.getCaption(), response.getCaption());
    }

    @Test
    @DisplayName("Should throw NotFoundException when post not found")
    void testGetPostById_NotFound() {
        // Arrange
        when(postRepository.findById(testPostId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> postService.getPostById(testPostId));
    }

    @Test
    @DisplayName("Should get posts by user ID with pagination")
    void testGetPostByUserId_Success() {
        // Arrange
        List<Post> posts = new ArrayList<>();
        posts.add(testPost);
        posts.add(Post.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .caption("Another post")
                .createdAt(LocalDateTime.now())
                .build());

        when(postRepository.findPostsByUserId(testUserId, 10, "cursor")).thenReturn(posts);

        // Act
        List<PostResponseDto> response = postService.getPostByUserId(testUserId, "cursor");

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    @DisplayName("Should get location post details")
    void testGetLocationPost_Success() {
        // Arrange
        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));

        // Act
        LocationPostResponseDto response = postService.getLocationPost(testPostId);

        // Assert
        assertNotNull(response);
        assertEquals(10.7769, response.getLatitude());
        assertEquals(106.7009, response.getLongitude());
    }

    @Test
    @DisplayName("Should send emoji reaction to post")
    void testSendEmoji_Success() {
        // Arrange
        ReactEmojiToPostRequestDto request = new ReactEmojiToPostRequestDto();
        request.setPostId(testPostId);
        request.setEmoji("👍");

        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));

        // Act
        assertDoesNotThrow(() -> postService.sendEmoji(request));

        // Assert
        verify(postRepository, times(1)).findById(testPostId);
    }

    @Test
    @DisplayName("Should drop emoji reaction from post")
    void testDropEmoji_Success() {
        // Arrange
        ReactEmojiToPostRequestDto request = new ReactEmojiToPostRequestDto();
        request.setPostId(testPostId);
        request.setEmoji("👍");

        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));

        // Act
        assertDoesNotThrow(() -> postService.dropEmoji(request));

        // Assert
        verify(postRepository, times(1)).findById(testPostId);
    }

    @Test
    @DisplayName("Should get emoji posts by post IDs")
    void testGetEmojiPosts_Success() {
        // Arrange
        List<UUID> postIds = new ArrayList<>();
        postIds.add(testPostId);
        postIds.add(UUID.randomUUID());

        List<EmojiPostResponseDto> emojiPosts = new ArrayList<>();
        emojiPosts.add(EmojiPostResponseDto.builder()
                .postId(testPostId)
                .emoji("👍")
                .count(5)
                .build());

        when(postRepository.getEmojisByPostIds(postIds)).thenReturn(emojiPosts);

        // Act
        List<EmojiPostResponseDto> response = postService.getEmojiPosts(postIds);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Should update post mode location")
    void testUpdateModeLocationPostById_Success() {
        // Arrange
        PostModeLocation newMode = PostModeLocation.PRIVATE;
        when(postRepository.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        assertDoesNotThrow(() -> postService.updateModeLocationPostById(testPostId, newMode));

        // Assert
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should get friend posts with pagination")
    void testGetFriendPosts_Success() {
        // Arrange
        List<Post> friendPosts = new ArrayList<>();
        friendPosts.add(testPost);

        when(postRepository.findFriendPostsByUserId(testUserId, 10, "cursor")).thenReturn(friendPosts);

        // Act
        List<PostResponseDto> response = postService.getFriendPosts(testUserId, "cursor");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
    }
}
