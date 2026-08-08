package com.example.app.integration;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.repository.main.UserRepository;
import com.example.lockly.service.RedisService;
import com.example.lockly.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private RedisService redisService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("password-hash")
                .displayName("Test User")
                .mode(UserMode.PUBLIC)
                .build();

        user = userRepository.save(user);
    }

    @Test
    void updateModeByUserId_ShouldUpdateModeToPrivate() {

        userService.updateModeByUserId(
                user,
                UserMode.PRIVATE
        );

        User savedUser = userRepository
                .findById(user.getId())
                .orElseThrow();

        assertThat(savedUser.getMode())
                .isEqualTo(UserMode.PRIVATE);

        verify(redisService)
                .saveUser(any());
    }

    @Test
    void updateModeByUserId_ShouldUpdateModeToPublic() {

        userService.updateModeByUserId(
                user,
                UserMode.PRIVATE
        );

        userService.updateModeByUserId(
                user,
                UserMode.PUBLIC
        );

        User savedUser = userRepository
                .findById(user.getId())
                .orElseThrow();

        assertThat(savedUser.getMode())
                .isEqualTo(UserMode.PUBLIC);

        verify(redisService)
                .saveUser(any());
    }
}
