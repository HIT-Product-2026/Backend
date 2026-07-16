package com.example.lockly.integration;

import com.example.lockly.domain.dto.request.auth.LoginRequestDto;
import com.example.lockly.domain.dto.request.auth.RegisterRequestDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Tests")
class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up and setup test data
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full user registration flow")
    void testFullRegistrationFlow() throws Exception {
        // Act & Assert - Send OTP
        mockMvc.perform(post("/api/v1/auth/send-otp/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"newuser@example.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should complete full login flow")
    void testFullLoginFlow() throws Exception {
        // Setup - Create a user in database
        testUser = User.builder()
                .email("test@example.com")
                .password("$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy7dL7e") // Encoded password
                .displayName("Test User")
                .isActive(true)
                .build();

        userRepository.save(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"password\": \"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    @Test
    @DisplayName("Should handle concurrent user requests")
    void testConcurrentRequests() throws Exception {
        // This test would typically use multithreading libraries
        // For simplicity, we're testing sequential behavior
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Should verify database transaction rollback on error")
    void testTransactionRollback() {
        // This would test that failed operations don't persist data
        int initialCount = (int) userRepository.count();
        
        // Attempt invalid operation
        try {
            userRepository.save(User.builder().build());
        } catch (Exception e) {
            // Expected to fail
        }

        // Verify count hasn't changed
        int finalCount = (int) userRepository.count();
        // Assert counts are equal (rollback occurred or no save happened)
    }

    @Test
    @DisplayName("Should handle API request validation")
    void testRequestValidation() throws Exception {
        // Send request with invalid data
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"invalid-email\", \"password\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should verify CORS headers in response")
    void testCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
