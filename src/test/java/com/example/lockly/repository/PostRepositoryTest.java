package com.example.lockly.repository;

import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.PostModeLocation;
import com.example.lockly.repository.main.PostRepository;
import com.example.lockly.repository.main.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PostRepository Integration Tests")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Post testPost;
    private User testUser;
    private UUID testPostId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .displayName("Test User")
                .isActive(true)
                .build();

        entityManager.persistAndFlush(testUser);

        testPostId = UUID.randomUUID();
        testPost = Post.builder()
                .id(testPostId)
                .user(testUser)
                .caption("Test post caption")
                .imageUrl("https://example.com/image.jpg")
                .latitude(10.7769)
                .longitude(106.7009)
                .createdAt(LocalDateTime.now())
                .modeLocation(PostModeLocation.PUBLIC)
                .build();

        entityManager.persistAndFlush(testPost);
    }

    @Test
    @DisplayName("Should find post by ID")
    void testFindById_Success() {
        // Act
        Optional<Post> result = postRepository.findById(testPostId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test post caption", result.get().getCaption());
    }

    @Test
    @DisplayName("Should return empty when post not found")
    void testFindById_NotFound() {
        // Act
        Optional<Post> result = postRepository.findById(UUID.randomUUID());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find all posts by user ID")
    void testFindPostsByUserId() {
        // Arrange
        Post post2 = Post.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .caption("Another post")
                .createdAt(LocalDateTime.now())
                .modeLocation(PostModeLocation.PUBLIC)
                .build();

        entityManager.persistAndFlush(post2);

        // Act
        List<Post> results = postRepository.findByUserId(testUserId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Should find posts by location with radius")
    void testFindPostsByLocation() {
        // Arrange - Posts with different locations
        Post post2 = Post.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .caption("Post near location")
                .latitude(10.7780)
                .longitude(106.7010)
                .modeLocation(PostModeLocation.PUBLIC)
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(post2);

        // Act
        List<Post> results = postRepository.findPostsByLocationWithRadius(10.7769, 106.7009, 1000);

        // Assert
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("Should find public posts only")
    void testFindPublicPosts() {
        // Arrange
        Post privatePost = Post.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .caption("Private post")
                .modeLocation(PostModeLocation.PRIVATE)
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(privatePost);

        // Act
        List<Post> publicPosts = postRepository.findByModeLocation(PostModeLocation.PUBLIC);

        // Assert
        assertTrue(publicPosts.stream().allMatch(p -> p.getModeLocation() == PostModeLocation.PUBLIC));
    }

    @Test
    @DisplayName("Should count posts by user")
    void testCountPostsByUser() {
        // Act
        long count = postRepository.countByUserId(testUserId);

        // Assert
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should update post caption")
    void testUpdatePostCaption() {
        // Arrange
        testPost.setCaption("Updated caption");

        // Act
        Post updatedPost = postRepository.save(testPost);

        // Assert
        assertEquals("Updated caption", updatedPost.getCaption());
    }

    @Test
    @DisplayName("Should update post mode location")
    void testUpdatePostModeLocation() {
        // Arrange
        testPost.setModeLocation(PostModeLocation.PRIVATE);

        // Act
        Post updatedPost = postRepository.save(testPost);

        // Assert
        assertEquals(PostModeLocation.PRIVATE, updatedPost.getModeLocation());
    }

    @Test
    @DisplayName("Should delete post by ID")
    void testDeleteById() {
        // Act
        postRepository.deleteById(testPostId);
        Optional<Post> result = postRepository.findById(testPostId);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find posts created within date range")
    void testFindPostsByDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // Act
        List<Post> results = postRepository.findByCreatedAtBetween(startDate, endDate);

        // Assert
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("Should verify post belongs to user")
    void testPostBelongsToUser() {
        // Act
        Post post = postRepository.findById(testPostId).orElseThrow();

        // Assert
        assertEquals(testUserId, post.getUser().getId());
    }
}
