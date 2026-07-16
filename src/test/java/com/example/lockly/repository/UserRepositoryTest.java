package com.example.lockly.repository;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.UserMode;
import com.example.lockly.repository.main.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .password("hashedPassword123")
                .displayName("Test User")
                .latitude(10.7769)
                .longitude(106.7009)
                .isActive(true)
                .userMode(UserMode.PUBLIC)
                .build();

        entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail_Success() {
        // Act
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("Test User", result.get().getDisplayName());
    }

    @Test
    @DisplayName("Should return empty when user email not found")
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find user by ID")
    void testFindById_Success() {
        // Act
        Optional<User> result = userRepository.findById(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getId());
    }

    @Test
    @DisplayName("Should search users by display name keyword")
    void testSearchFriendByKeyword_Success() {
        // Arrange
        User friend1 = User.builder()
                .id(UUID.randomUUID())
                .email("friend1@example.com")
                .displayName("Test Friend")
                .isActive(true)
                .build();

        User friend2 = User.builder()
                .id(UUID.randomUUID())
                .email("friend2@example.com")
                .displayName("Another Friend")
                .isActive(true)
                .build();

        entityManager.persistAndFlush(friend1);
        entityManager.persistAndFlush(friend2);

        // Act
        List<User> results = userRepository.searchFriendByKeyword("Test");

        // Assert
        assertNotNull(results);
        assertTrue(results.stream().anyMatch(u -> u.getDisplayName().contains("Test")));
    }

    @Test
    @DisplayName("Should find active users only")
    void testFindActiveUsers() {
        // Arrange
        User inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .email("inactive@example.com")
                .displayName("Inactive User")
                .isActive(false)
                .build();

        entityManager.persistAndFlush(inactiveUser);

        // Act
        List<User> activeUsers = userRepository.findByIsActive(true);

        // Assert
        assertTrue(activeUsers.stream().allMatch(User::getIsActive));
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void testExistsByEmail() {
        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("notexist@example.com");

        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should update user display name")
    void testUpdateUserDisplayName() {
        // Arrange
        testUser.setDisplayName("Updated Name");

        // Act
        User updatedUser = userRepository.save(testUser);

        // Assert
        assertEquals("Updated Name", updatedUser.getDisplayName());
    }

    @Test
    @DisplayName("Should update user location")
    void testUpdateUserLocation() {
        // Arrange
        testUser.setLatitude(21.0285);
        testUser.setLongitude(105.8542);

        // Act
        User updatedUser = userRepository.save(testUser);

        // Assert
        assertEquals(21.0285, updatedUser.getLatitude());
        assertEquals(105.8542, updatedUser.getLongitude());
    }

    @Test
    @DisplayName("Should update user mode")
    void testUpdateUserMode() {
        // Arrange
        testUser.setUserMode(UserMode.PRIVATE);

        // Act
        User updatedUser = userRepository.save(testUser);

        // Assert
        assertEquals(UserMode.PRIVATE, updatedUser.getUserMode());
    }

    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteById() {
        // Act
        userRepository.deleteById(testUserId);
        Optional<User> result = userRepository.findById(testUserId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should count total users")
    void testCountUsers() {
        // Act
        long count = userRepository.count();

        // Assert
        assertTrue(count > 0);
    }
}
