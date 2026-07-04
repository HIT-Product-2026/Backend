package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostsRepository extends JpaRepository<Post, UUID> {
    @Query("""
    SELECT p
    FROM Post p
    JOIN FETCH p.user
    WHERE p.user = :user
    ORDER BY p.createdAt DESC
""")
    Page<Post> findByUserOrderByCreatedAtDesc(
            @Param("user") User user,
            Pageable pageable
    );

    Page<Post> findByUserIdInOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);

    int countByUserId(UUID userId);
}
