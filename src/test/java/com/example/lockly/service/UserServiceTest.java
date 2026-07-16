package com.example.lockly.service;

import com.example.lockly.domain.dto.response.common.UserResponseDto;
import com.example.lockly.domain.dto.response.common.UserSimpleResponseDto;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.exception.nonRetryException.NotFoundException;
import com.example.lockly.repository.main.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MinIOService minIOService;

    @MockBean
    private RedisService redisService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .latitude(10.7769)
                .longitude(106.7009)
                .isActive(true)
                .userMode(UserMode.PUBLIC)
                .build();
    }

    @Test
    @DisplayName("Should find friends by user ID")
    void testFindFriendsByUserId_Success() {
        // Arrange
        UUID friendId1 = UUID.randomUUID();
        UUID friendId2 = UUID.randomUUID();
        List<User> friends = new ArrayList<>();
        friends.add(User.builder().id(friendId1).displayName("Friend 1").build());
        friends.add(User.builder().id(friendId2).displayName("Friend 2").build());

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findFriendsByUserId(testUserId)).thenReturn(friends);

        // Act
        List<UserResponseDto> result = userService.findFriendsByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findFriendsByUserId(testUserId);
    }

    @Test
    @DisplayName("Should search friends by keyword")
    void testSearchFriend_Success() {
        // Arrange
        List<User> searchResults = new ArrayList<>();
        searchResults.add(User.builder().id(UUID.randomUUID()).displayName("Test Friend").build());

        when(userRepository.searchFriendByKeyword(anyString())).thenReturn(searchResults);

        // Act
        List<UserSimpleResponseDto> result = userService.searchFriend("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should check if user is online")
    void testIsUserOnlineByUserId() {
        // Arrange
        when(redisService.isUserOnline(testUserId.toString())).thenReturn(true);

        // Act
        boolean isOnline = userService.isUserOnlineByUserId(testUserId);

        // Assert
        assertTrue(isOnline);
    }

    @Test
    @DisplayName("Should check if user is friend")
    void testIsFriendByUserId_True() {
        // Arrange
        UUID friendId = UUID.randomUUID();
        when(userRepository.isFriend(testUserId, friendId)).thenReturn(true);

        // Act
        boolean isFriend = userService.isFriendByUserId(testUserId, friendId);

        // Assert
        assertTrue(isFriend);
    }

    @Test
    @DisplayName("Should return false when checking non-friend user")
    void testIsFriendByUserId_False() {
        // Arrange
        UUID nonFriendId = UUID.randomUUID();
        when(userRepository.isFriend(testUserId, nonFriendId)).thenReturn(false);

        // Act
        boolean isFriend = userService.isFriendByUserId(testUserId, nonFriendId);

        // Assert
        assertFalse(isFriend);
    }

    @Test
    @DisplayName("Should update user location")
    void testUpdateUserLocationByUserId_Success() {
        // Arrange
        Double newLatitude = 10.8000;
        Double newLongitude = 106.8000;

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> userService.updateUserLocationByUserId(testUserId, newLatitude, newLongitude));

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update display name")
    void testUpdateDisplayNameByUserId_Success() {
        // Arrange
        String newDisplayName = "Updated Name";
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> userService.updateDisplayNameByUserId(testUserId, newDisplayName));

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user mode")
    void testUpdateModeByUserId_Success() {
        // Arrange
        UserMode newMode = UserMode.PRIVATE;
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> userService.updateModeByUserId(testUserId, newMode));

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found")
    void testUpdateDisplayName_UserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, 
                () -> userService.updateDisplayNameByUserId(testUserId, "New Name"));
    }

    @Test
    @DisplayName("Should update FCM token")
    void testUpdateFcmTokenByUserId_Success() {
        // Arrange
        String newFcmToken = "new_fcm_token_123";
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> userService.updateFcmTokenByUserId(testUserId, newFcmToken));

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should find FCM tokens of friends")
    void testFindFcmTokenOfFriendsByUserId_Success() {
        // Arrange
        List<String> fcmTokens = new ArrayList<>();
        fcmTokens.add("token_1");
        fcmTokens.add("token_2");

        when(userRepository.findFcmTokenOfFriendsByUserId(testUserId)).thenReturn(fcmTokens);

        // Act
        List<String> result = userService.findFcmTokenOfFriendsByUserId(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
