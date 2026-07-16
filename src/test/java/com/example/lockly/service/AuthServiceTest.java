package com.example.lockly.service;

import com.example.lockly.domain.dto.request.auth.*;
import com.example.lockly.domain.dto.response.LoginResponseDto;
import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.UnauthorizedException;
import com.example.lockly.repository.main.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .password("encodedPassword123")
                .displayName("Test User")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully register user with valid OTP")
    void testVerifyOtpAndRegister_Success() {
        // Arrange
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setEmail("newuser@example.com");
        request.setOtp("123456");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponseDto response = authService.verifyOtpAndRegister(request);

        // Assert
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void testVerifyOtpAndRegister_DuplicateEmail() {
        // Arrange
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setEmail("test@example.com");
        request.setOtp("123456");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> authService.verifyOtpAndRegister(request));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // Act
        LoginResponseDto response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
    }

    @Test
    @DisplayName("Should throw exception when login with invalid password")
    void testLogin_InvalidPassword() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should throw exception when login with non-existent email")
    void testLogin_UserNotFound() {
        // Arrange
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should send OTP for password reset")
    void testSendOtpForForgotPassword_Success() {
        // Arrange
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> authService.sendOtpForForgotPassword(request));
    }

    @Test
    @DisplayName("Should reset password with valid OTP")
    void testResetPassword_Success() {
        // Arrange
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setEmail("test@example.com");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> authService.resetPassword(request));
    }

    @Test
    @DisplayName("Should successfully logout")
    void testLogout_Success() {
        // Arrange
        LogoutRequestDto request = new LogoutRequestDto();
        request.setToken("validToken");

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> authService.logout(request));
    }
}
