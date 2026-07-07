package com.example.lockly.repository.main;

import com.example.lockly.domain.dto.response.PostEmojiCount;
import com.example.lockly.domain.entity.main.EmojiPost;
import com.example.lockly.domain.entity.main.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Query("""
    SELECT ep.post
    FROM EmojiPost ep
    WHERE ep.post.user.id = :userId
      AND ep.post.createdAt >= :from
    GROUP BY ep.post
    ORDER BY COUNT(ep) DESC, ep.post.createdAt DESC
""")
    List<Post> findPostsOrderByEmojiCountDesc(
            @Param("userId") UUID userId,
            @Param("from") LocalDateTime from,
            Pageable pageable
    );

    long countByPost(Post post);

    @Query("""
SELECT new com.example.lockly.dto.PostEmojiCount(
    ep.post,
    COUNT(ep)
)
FROM EmojiPost ep
WHERE ep.post.user.id = :userId
  AND ep.post.createdAt >= :from
GROUP BY ep.post
ORDER BY COUNT(ep) DESC
""")
    List<PostEmojiCount> findTopPostWithEmojiCount(
            @Param("userId") UUID userId,
            @Param("from") LocalDateTime from,
            Pageable pageable
    );
}
