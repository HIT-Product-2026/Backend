package com.example.lockly.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for integration tests.
 * Provides test beans and mocks for testing environment.
 */
@TestConfiguration
public class TestConfiguration {

    /**
     * Provides a real PasswordEncoder for tests instead of mocking.
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides a mock RedisTemplate for tests.
     * This prevents actual Redis connection during tests.
     */
    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> testRedisTemplate() {
        return mock(RedisTemplate.class);
    }
}
