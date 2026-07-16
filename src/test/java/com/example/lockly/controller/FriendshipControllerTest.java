package com.example.lockly.controller;

import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.dto.response.common.FriendshipsResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.service.AuthService;
import com.example.lockly.service.FriendshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("FriendshipController Integration Tests")
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendshipService friendshipService;

    @MockBean
    private AuthService authService;

    private User testUser;
    private User targetUser;
    private UUID testUserId;
    private UUID targetUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .isActive(true)
                .build();

        targetUser = User.builder()
                .id(targetUserId)
                .email("target@example.com")
                .displayName("Target User")
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should send friend request successfully")
    void testSendFriendRequest_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(friendshipService).sendFriendRequest(testUserId, targetUserId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/friendship/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetUserId\": \"" + targetUserId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should accept friend request")
    void testAcceptFriendRequest_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(friendshipService).acceptFriendRequest(testUserId, targetUserId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/friendship/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requesterId\": \"" + targetUserId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should reject friend request")
    void testRejectFriendRequest_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(friendshipService).rejectFriendRequest(testUserId, targetUserId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/friendship/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requesterId\": \"" + targetUserId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should remove friend")
    void testRemoveFriend_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(friendshipService).removeFriend(testUserId, targetUserId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/friendship/" + targetUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get friend requests")
    void testGetFriendRequests_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/friendship/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get friend suggestions")
    void testGetFriendSuggestions_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/v1/friendship/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should block user")
    void testBlockUser_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(friendshipService).blockUser(testUserId, targetUserId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/friendship/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blockedUserId\": \"" + targetUserId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should unblock user")
    void testUnblockUser_Success() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(friendshipService).unblockUser(testUserId, targetUserId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/friendship/unblock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blockedUserId\": \"" + targetUserId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }
}
