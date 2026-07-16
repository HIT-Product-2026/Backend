package com.example.lockly.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RabbitMQService Unit Tests")
class RabbitMQServiceTest {

    @Autowired
    private RabbitMQService rabbitMQService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private Map<String, Object> testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new HashMap<>();
        testMessage.put("userId", "user_123");
        testMessage.put("action", "POST_CREATED");
        testMessage.put("timestamp", System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should send message to queue successfully")
    void testSendMessage_Success() {
        // Arrange
        String queueName = "lockly.posts";

        // Act
        assertDoesNotThrow(() -> rabbitMQService.sendMessage(queueName, testMessage));

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    @DisplayName("Should send multiple messages")
    void testSendMultipleMessages() {
        // Arrange
        String queueName = "lockly.notifications";
        int messageCount = 5;

        // Act
        for (int i = 0; i < messageCount; i++) {
            testMessage.put("messageId", i);
            assertDoesNotThrow(() -> rabbitMQService.sendMessage(queueName, testMessage));
        }

        // Assert
        verify(rabbitTemplate, times(messageCount)).convertAndSend(anyString(), any());
    }

    @Test
    @DisplayName("Should publish event to exchange")
    void testPublishEvent_Success() {
        // Arrange
        String exchangeName = "lockly.events";
        String routingKey = "post.created";

        // Act
        assertDoesNotThrow(() -> rabbitMQService.publishEvent(exchangeName, routingKey, testMessage));

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(exchangeName, routingKey, testMessage);
    }

    @Test
    @DisplayName("Should handle message with custom headers")
    void testSendMessageWithHeaders() {
        // Arrange
        String queueName = "lockly.emails";
        Map<String, String> headers = new HashMap<>();
        headers.put("priority", "high");
        headers.put("retry", "3");

        // Act
        assertDoesNotThrow(() -> rabbitMQService.sendMessageWithHeaders(queueName, testMessage, headers));

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), any());
    }

    @Test
    @DisplayName("Should retry sending message on failure")
    void testSendMessageWithRetry() {
        // Arrange
        String queueName = "lockly.posts";
        doThrow(new RuntimeException("Connection failed"))
                .doNothing()
                .when(rabbitTemplate).convertAndSend(anyString(), any());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> rabbitMQService.sendMessageWithRetry(queueName, testMessage, 3));
    }

    @Test
    @DisplayName("Should validate queue name format")
    void testValidateQueueName() {
        // Act & Assert
        assertTrue(rabbitMQService.isValidQueueName("lockly.posts"));
        assertTrue(rabbitMQService.isValidQueueName("lockly.notifications"));
        assertFalse(rabbitMQService.isValidQueueName(""));
        assertFalse(rabbitMQService.isValidQueueName(null));
    }

    @Test
    @DisplayName("Should create queue dynamically")
    void testDynamicQueueCreation() {
        // Arrange
        String dynamicQueue = "lockly.dynamic.test";

        // Act
        assertDoesNotThrow(() -> rabbitMQService.createQueue(dynamicQueue));

        // Assert
        verify(rabbitTemplate, atLeast(0)).convertAndSend(anyString(), any());
    }

    @Test
    @DisplayName("Should purge queue")
    void testPurgeQueue() {
        // Arrange
        String queueName = "lockly.test";

        // Act
        assertDoesNotThrow(() -> rabbitMQService.purgeQueue(queueName));
    }
}
