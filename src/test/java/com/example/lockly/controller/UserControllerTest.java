package com.example.lockly.controller;

import com.example.lockly.common.response.ApiResponse;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.service.AuthService;
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
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .userMode(UserMode.PUBLIC)
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should update avatar successfully")
    void testUpdateAvatar_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(userService).updateAvatarByUserId(testUserId, file);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/user/avatar")
                .file(file)
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")))
                .andExpect(jsonPath("$.message", containsString("thành công")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get user profile successfully")
    void testGetUserProfile_Success() throws Exception {
        // Arrange
        UserResponseDto userResponse = UserResponseDto.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .userMode(UserMode.PUBLIC)
                .build();

        when(authService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should update display name successfully")
    void testUpdateDisplayName_Success() throws Exception {
        // Arrange
        String newDisplayName = "Updated Name";
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(userService).updateDisplayNameByUserId(testUserId, newDisplayName);

        // Act & Assert
        mockMvc.perform(put("/api/v1/user/display-name")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\": \"" + newDisplayName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should update user mode successfully")
    void testUpdateUserMode_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(userService).updateModeByUserId(testUserId, UserMode.PRIVATE);

        // Act & Assert
        mockMvc.perform(put("/api/v1/user/mode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mode\": \"PRIVATE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should search friends successfully")
    void testSearchFriend_Success() throws Exception {
        // Arrange
        String keyword = "Test";

        // Act & Assert
        mockMvc.perform(get("/api/v1/user/search")
                .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get friends list successfully")
    void testGetFriends_Success() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(get("/api/v1/user/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return 401 when not authenticated")
    void testWithoutAuthentication_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/user/friends")
                .with(request -> {
                    request.getSession().invalidate();
                    return request;
                }))
                .andExpect(status().isUnauthorized());
    }
}
