package com.example.lockly.repository;

import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostsRepository extends JpaRepository<Post, String> {
    @Query("""
    SELECT p
    FROM Post p
    JOIN FETCH p.user
    WHERE p.user = :user
    ORDER BY p.createdAt DESC
""")
    List<Post> findByUserOrderByCreatedAtDesc(
            @Param("user") User user
    );
}
