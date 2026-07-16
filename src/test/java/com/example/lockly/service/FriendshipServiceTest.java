package com.example.lockly.service;

import com.example.lockly.domain.dto.request.create.CreateFriendshipRequestDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.exception.nonRetryException.BadRequestException;
import com.example.lockly.exception.nonRetryException.NotFoundException;
import com.example.lockly.repository.main.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FriendshipService Unit Tests")
class FriendshipServiceTest {

    @Autowired
    private FriendshipService friendshipService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private User friendUser;
    private UUID testUserId;
    private UUID friendUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        friendUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .isActive(true)
                .build();

        friendUser = User.builder()
                .id(friendUserId)
                .email("friend@example.com")
                .displayName("Friend User")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should send friend request successfully")
    void testSendFriendRequest_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUserId)).thenReturn(Optional.of(friendUser));

        // Act
        assertDoesNotThrow(() -> friendshipService.sendFriendRequest(testUserId, friendUserId));

        // Assert
        verify(userRepository, times(2)).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when sending request to non-existent user")
    void testSendFriendRequest_UserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, 
                () -> friendshipService.sendFriendRequest(testUserId, friendUserId));
    }

    @Test
    @DisplayName("Should throw exception when sending request to self")
    void testSendFriendRequest_SelfRequest() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadRequestException.class, 
                () -> friendshipService.sendFriendRequest(testUserId, testUserId));
    }

    @Test
    @DisplayName("Should accept friend request successfully")
    void testAcceptFriendRequest_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUserId)).thenReturn(Optional.of(friendUser));

        // Act
        assertDoesNotThrow(() -> friendshipService.acceptFriendRequest(testUserId, friendUserId));

        // Assert
        verify(userRepository, times(2)).findById(any());
    }

    @Test
    @DisplayName("Should reject friend request successfully")
    void testRejectFriendRequest_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUserId)).thenReturn(Optional.of(friendUser));

        // Act
        assertDoesNotThrow(() -> friendshipService.rejectFriendRequest(testUserId, friendUserId));

        // Assert
        verify(userRepository, times(2)).findById(any());
    }

    @Test
    @DisplayName("Should remove friend successfully")
    void testRemoveFriend_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUserId)).thenReturn(Optional.of(friendUser));

        // Act
        assertDoesNotThrow(() -> friendshipService.removeFriend(testUserId, friendUserId));

        // Assert
        verify(userRepository, times(2)).findById(any());
    }

    @Test
    @DisplayName("Should block user successfully")
    void testBlockUser_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUserId)).thenReturn(Optional.of(friendUser));

        // Act
        assertDoesNotThrow(() -> friendshipService.blockUser(testUserId, friendUserId));

        // Assert
        verify(userRepository, times(2)).findById(any());
    }

    @Test
    @DisplayName("Should unblock user successfully")
    void testUnblockUser_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(friendUserId)).thenReturn(Optional.of(friendUser));

        // Act
        assertDoesNotThrow(() -> friendshipService.unblockUser(testUserId, friendUserId));

        // Assert
        verify(userRepository, times(2)).findById(any());
    }
}
