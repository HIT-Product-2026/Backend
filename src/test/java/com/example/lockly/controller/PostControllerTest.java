package com.example.lockly.controller;

import com.example.lockly.domain.dto.response.common.PostResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.PostService;
import com.example.lockly.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PostController Integration Tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    private User testUser;
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
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should create post successfully with image")
    void testCreatePost_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        PostResponseDto postResponse = PostResponseDto.builder()
                .id(testPostId)
                .caption("Test post")
                .latitude(10.7769)
                .longitude(106.7009)
                .createdAt(LocalDateTime.now())
                .modeLocation(PostModeLocation.PUBLIC)
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(postService.createPost(any())).thenReturn(postResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/post")
                .file(file)
                .param("caption", "Test post")
                .param("longitude", "106.7009")
                .param("latitude", "10.7769")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")))
                .andExpect(jsonPath("$.data.caption", equalTo("Test post")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get post by ID")
    void testGetPostById_Success() throws Exception {
        // Arrange
        PostResponseDto postResponse = PostResponseDto.builder()
                .id(testPostId)
                .caption("Test post")
                .build();

        when(postService.getPostById(testPostId)).thenReturn(postResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/post/" + testPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")))
                .andExpect(jsonPath("$.data.caption", equalTo("Test post")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get user posts with pagination")
    void testGetUserPosts_Success() throws Exception {
        // Arrange
        List<PostResponseDto> posts = new ArrayList<>();
        posts.add(PostResponseDto.builder()
                .id(testPostId)
                .caption("Post 1")
                .build());

        when(postService.getPostByUserId(testUserId, null)).thenReturn(posts);

        // Act & Assert
        mockMvc.perform(get("/api/v1/post/user/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")))
                .andExpect(jsonPath("$.data[0].caption", equalTo("Post 1")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get friend posts")
    void testGetFriendPosts_Success() throws Exception {
        // Arrange
        List<PostResponseDto> friendPosts = new ArrayList<>();
        friendPosts.add(PostResponseDto.builder()
                .id(UUID.randomUUID())
                .caption("Friend post")
                .build());

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(postService.getFriendPosts(testUserId, null)).thenReturn(friendPosts);

        // Act & Assert
        mockMvc.perform(get("/api/v1/post/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")))
                .andExpect(jsonPath("$.data[0].caption", equalTo("Friend post")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should react with emoji")
    void testReactEmoji_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(postService).sendEmoji(any());

        // Act & Assert
        mockMvc.perform(post("/api/v1/post/emoji")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"postId\": \"" + testPostId + "\", \"emoji\": \"👍\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should remove emoji reaction")
    void testRemoveEmoji_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(postService).dropEmoji(any());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/post/emoji")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"postId\": \"" + testPostId + "\", \"emoji\": \"👍\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should update post mode location")
    void testUpdatePostModeLocation_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(postService).updateModeLocationPostById(testPostId, PostModeLocation.PRIVATE);

        // Act & Assert
        mockMvc.perform(put("/api/v1/post/" + testPostId + "/mode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modeLocation\": \"PRIVATE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @DisplayName("Should return 401 without authentication")
    void testCreatePost_Unauthorized() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/post")
                .file(file)
                .param("caption", "Test post")
                .param("longitude", "106.7009")
                .param("latitude", "10.7769"))
                .andExpect(status().isUnauthorized());
    }
}
