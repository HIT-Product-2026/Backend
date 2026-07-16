package com.example.lockly.controller;

import com.example.lockly.domain.dto.request.auth.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.service.AuthService;
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
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should send OTP for registration")
    void testSendOtpForRegister_Success() throws Exception {
        // Arrange
        String email = "newuser@example.com";
        doNothing().when(authService).sendOtpForRegister(any(RegisterRequestDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/send-otp/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @DisplayName("Should verify OTP and register successfully")
    void testVerifyOtpAndRegister_Success() throws Exception {
        // Arrange
        UserResponseDto userResponse = UserResponseDto.builder()
                .id(testUserId)
                .email("newuser@example.com")
                .displayName("New User")
                .build();

        when(authService.verifyOtpAndRegister(any(VerifyOtpRequestDto.class)))
                .thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/verify-otp/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"newuser@example.com\", \"otp\": \"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")))
                .andExpect(jsonPath("$.data.email", equalTo("newuser@example.com")));
    }

    @Test
    @DisplayName("Should login successfully")
    void testLogin_Success() throws Exception {
        // Arrange
        LoginResponseDto loginResponse = LoginResponseDto.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .refreshToken("refresh_token_123")
                .build();

        when(authService.login(any(LoginRequestDto.class)))
                .thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"password\": \"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should logout successfully")
    void testLogout_Success() throws Exception {
        // Arrange
        doNothing().when(authService).logout(any(LogoutRequestDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"access_token_123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @DisplayName("Should send OTP for forgot password")
    void testSendOtpForForgotPassword_Success() throws Exception {
        // Arrange
        doNothing().when(authService).sendOtpForForgotPassword(any(ForgotPasswordRequestDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/send-otp/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @DisplayName("Should verify OTP for forgot password")
    void testVerifyOtpForgotPassword_Success() throws Exception {
        // Arrange
        doNothing().when(authService).verifyOtpForgotPassword(any(VerifyOtpRequestDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/verify-otp/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"otp\": \"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @DisplayName("Should reset password successfully")
    void testResetPassword_Success() throws Exception {
        // Arrange
        doNothing().when(authService).resetPassword(any(ResetPasswordRequestDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"newPassword\": \"newPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("200")));
    }

    @Test
    @DisplayName("Should return 400 on invalid email format")
    void testLogin_InvalidEmail() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"invalid-email\", \"password\": \"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 on empty password")
    void testLogin_EmptyPassword() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"password\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
