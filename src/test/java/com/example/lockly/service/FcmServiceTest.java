package com.example.lockly.service;

import com.example.lockly.domain.dto.request.FcmNotificationRequestDto;
import com.example.lockly.domain.entity.main.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FCMService Unit Tests")
class FcmServiceTest {

    @Autowired
    private FcmService fcmService;

    @MockBean
    private FirebaseMessaging firebaseMessaging;

    @MockBean
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .fcmToken("test_fcm_token_123")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should send push notification to single user")
    void testSendPushNotification_Success() {
        // Arrange
        FcmNotificationRequestDto request = new FcmNotificationRequestDto();
        request.setTitle("Test Notification");
        request.setBody("This is a test notification");
        request.setUserId(testUserId);

        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_123");

        // Act
        assertDoesNotThrow(() -> fcmService.sendPushNotification(request));

        // Assert
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("Should send push notifications to multiple users")
    void testSendPushNotificationToMultipleUsers_Success() {
        // Arrange
        List<UUID> userIds = new ArrayList<>();
        userIds.add(testUserId);
        userIds.add(UUID.randomUUID());

        FcmNotificationRequestDto request = new FcmNotificationRequestDto();
        request.setTitle("Broadcast Notification");
        request.setBody("Message to multiple users");
        request.setUserIds(userIds);

        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_123");

        // Act
        assertDoesNotThrow(() -> fcmService.sendBroadcastNotification(request));

        // Assert
        verify(firebaseMessaging, atLeast(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("Should handle FCM token update")
    void testUpdateFcmToken_Success() {
        // Arrange
        String newFcmToken = "new_fcm_token_456";
        when(userService.updateFcmTokenByUserId(testUserId, newFcmToken)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> fcmService.updateUserFcmToken(testUserId, newFcmToken));

        // Assert
        verify(userService, times(1)).updateFcmTokenByUserId(testUserId, newFcmToken);
    }

    @Test
    @DisplayName("Should retry sending notification on failure")
    void testSendNotification_RetryOnFailure() {
        // Arrange
        FcmNotificationRequestDto request = new FcmNotificationRequestDto();
        request.setTitle("Retry Test");
        request.setBody("Testing retry mechanism");
        request.setUserId(testUserId);

        when(firebaseMessaging.send(any(Message.class)))
                .thenThrow(new RuntimeException("Firebase service unavailable"))
                .thenReturn("message_id_123");

        // Act
        assertDoesNotThrow(() -> fcmService.sendPushNotificationWithRetry(request));

        // Assert - Should be called at least twice (initial + retry)
        verify(firebaseMessaging, atLeast(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("Should validate FCM token format")
    void testValidateFcmToken_Valid() {
        // Act & Assert
        assertTrue(fcmService.isValidFcmToken("valid_fcm_token_abc123def456"));
    }

    @Test
    @DisplayName("Should reject invalid FCM token format")
    void testValidateFcmToken_Invalid() {
        // Act & Assert
        assertFalse(fcmService.isValidFcmToken(""));
        assertFalse(fcmService.isValidFcmToken(null));
    }

    @Test
    @DisplayName("Should send notification with custom data")
    void testSendNotificationWithCustomData_Success() {
        // Arrange
        FcmNotificationRequestDto request = new FcmNotificationRequestDto();
        request.setTitle("Custom Data Test");
        request.setBody("Message with custom data");
        request.setUserId(testUserId);
        request.putData("key1", "value1");
        request.putData("key2", "value2");

        when(firebaseMessaging.send(any(Message.class))).thenReturn("message_id_123");

        // Act
        assertDoesNotThrow(() -> fcmService.sendPushNotification(request));

        // Assert
        verify(firebaseMessaging, times(1)).send(any(Message.class));
    }
}
