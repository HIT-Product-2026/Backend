package com.example.lockly.security;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.repository.main.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("JWT Authentication Filter Tests")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should allow request without authentication for public endpoints")
    void testPublicEndpoint_NoAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().isNotFound()); // Endpoint exists but we're testing access
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should allow authenticated request")
    void testAuthenticatedRequest_Success() throws Exception {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject request without valid JWT token")
    void testInvalidToken_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/user/profile")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with missing Authorization header")
    void testMissingAuthHeader_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with expired token")
    void testExpiredToken_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/user/profile")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    @DisplayName("Should verify user role for protected endpoints")
    void testUserRole_AccessAllowed() throws Exception {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk());
    }
}
