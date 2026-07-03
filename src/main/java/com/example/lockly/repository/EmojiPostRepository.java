package com.example.lockly.repository;

import com.example.lockly.domain.entity.EmojiPost;
import com.example.lockly.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmojiPostRepository extends JpaRepository<EmojiPost, UUID> {
    @Query("""
        SELECT ep
        FROM EmojiPost ep
        JOIN FETCH ep.sender s
        WHERE ep.post = :post
        ORDER BY ep.createdAt DESC
    """)
    List<EmojiPost> findByPostWithSenderOrderByCreatedAtDesc(Post post);

    @Query("""
    SELECT ep
    FROM EmojiPost ep
    JOIN FETCH ep.sender
    JOIN FETCH ep.post
    WHERE ep.post.id IN :postIds
    ORDER BY ep.createdAt DESC
""")
    List<EmojiPost> findByPostIdsWithSender(
            @Param("postIds") List<UUID> postIds
    );

    Optional<EmojiPost> findByPostIdAndSenderId(UUID postId, UUID userId);
}
