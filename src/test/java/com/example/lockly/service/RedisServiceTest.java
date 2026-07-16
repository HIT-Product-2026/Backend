package com.example.lockly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RedisService Unit Tests")
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Setup mock behavior
    }

    @Test
    @DisplayName("Should set value in Redis")
    void testSetValue_Success() {
        // Arrange
        String key = "test_key";
        String value = "test_value";
        long timeout = 3600;

        when(redisTemplate.opsForValue()).thenReturn(mock());

        // Act
        assertDoesNotThrow(() -> redisService.set(key, value, timeout, TimeUnit.SECONDS));

        // Assert
        verify(redisTemplate.opsForValue(), times(1)).set(key, value, timeout, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should get value from Redis")
    void testGetValue_Success() {
        // Arrange
        String key = "test_key";
        when(redisTemplate.opsForValue().get(key)).thenReturn("test_value");

        // Act
        Object result = redisService.get(key);

        // Assert
        assertNotNull(result);
        assertEquals("test_value", result);
    }

    @Test
    @DisplayName("Should return null when key not found")
    void testGetValue_KeyNotFound() {
        // Arrange
        String key = "nonexistent_key";
        when(redisTemplate.opsForValue().get(key)).thenReturn(null);

        // Act
        Object result = redisService.get(key);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should check if key exists")
    void testHasKey_Exists() {
        // Arrange
        String key = "test_key";
        when(redisTemplate.hasKey(key)).thenReturn(true);

        // Act
        boolean exists = redisService.hasKey(key);

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when key doesn't exist")
    void testHasKey_NotExists() {
        // Arrange
        String key = "nonexistent_key";
        when(redisTemplate.hasKey(key)).thenReturn(false);

        // Act
        boolean exists = redisService.hasKey(key);

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should delete key from Redis")
    void testDelete_Success() {
        // Arrange
        String key = "test_key";
        when(redisTemplate.delete(key)).thenReturn(true);

        // Act
        boolean deleted = redisService.delete(key);

        // Assert
        assertTrue(deleted);
        verify(redisTemplate, times(1)).delete(key);
    }

    @Test
    @DisplayName("Should set user online status")
    void testSetUserOnline_Success() {
        // Arrange
        String userId = "user_123";
        when(redisTemplate.opsForValue()).thenReturn(mock());

        // Act
        assertDoesNotThrow(() -> redisService.setUserOnline(userId));

        // Assert
        verify(redisTemplate.opsForValue(), times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should check if user is online")
    void testIsUserOnline_True() {
        // Arrange
        String userId = "user_123";
        when(redisTemplate.hasKey("online:" + userId)).thenReturn(true);

        // Act
        boolean isOnline = redisService.isUserOnline(userId);

        // Assert
        assertTrue(isOnline);
    }

    @Test
    @DisplayName("Should return false when user is not online")
    void testIsUserOnline_False() {
        // Arrange
        String userId = "user_123";
        when(redisTemplate.hasKey("online:" + userId)).thenReturn(false);

        // Act
        boolean isOnline = redisService.isUserOnline(userId);

        // Assert
        assertFalse(isOnline);
    }

    @Test
    @DisplayName("Should set user offline")
    void testSetUserOffline_Success() {
        // Arrange
        String userId = "user_123";
        when(redisTemplate.delete("online:" + userId)).thenReturn(true);

        // Act
        boolean offline = redisService.setUserOffline(userId);

        // Assert
        assertTrue(offline);
    }

    @Test
    @DisplayName("Should increment counter in Redis")
    void testIncrementCounter_Success() {
        // Arrange
        String key = "counter_key";
        when(redisTemplate.opsForValue().increment(key)).thenReturn(1L);

        // Act
        long result = redisService.increment(key);

        // Assert
        assertEquals(1, result);
    }
}
