package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.Post;
import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PostsRepository extends JpaRepository<Post, UUID> {

    @Query("""
    SELECT p
    FROM Post p
    JOIN FETCH p.user
    WHERE p.user = :user
    ORDER BY p.createdAt DESC, p.id DESC
    """)
    Slice<Post> findFirstPage(
            @Param("user") User user,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Post p
    JOIN FETCH p.user
    WHERE p.user = :user
    AND (
        p.createdAt < :cursorCreatedAt
        OR (
            p.createdAt = :cursorCreatedAt
            AND p.id < :cursorId
        )
    )
    ORDER BY p.createdAt DESC, p.id DESC
    """)
    Slice<Post> findNextPage(
            @Param("user") User user,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    int countByUserId(UUID userId);

    @Query("""
    SELECT p
    FROM Post p
    JOIN FETCH p.user
    WHERE p.user.id IN (
        SELECT CASE
            WHEN f.requester.id = :userId THEN f.receiver.id
            ELSE f.requester.id
        END
        FROM Friendship f
        WHERE (f.requester.id = :userId OR f.receiver.id = :userId)
          AND f.status = :status
    )
    ORDER BY p.createdAt DESC, p.id DESC
    """)
    Slice<Post> findFriendPostsFirstPage(
            UUID userId,
            FriendshipStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Post p
    JOIN FETCH p.user
    WHERE p.user.id IN (
        SELECT CASE
            WHEN f.requester.id = :userId THEN f.receiver.id
            ELSE f.requester.id
        END
        FROM Friendship f
        WHERE (f.requester.id = :userId OR f.receiver.id = :userId)
          AND f.status = :status
    )
    AND (
        p.createdAt < :cursorCreatedAt
        OR (
            p.createdAt = :cursorCreatedAt
            AND p.id < :cursorId
        )
    )
    ORDER BY p.createdAt DESC, p.id DESC
    """)
    Slice<Post> findFriendPostsNextPage(
            UUID userId,
            FriendshipStatus status,
            LocalDateTime cursorCreatedAt,
            UUID cursorId,
            Pageable pageable
    );

    @Query(value = """
    SELECT p.*
    FROM posts p
    
    WHERE p.user_id IN (
    
        SELECT
            CASE
                WHEN f.requester_id = :userId
                THEN f.receiver_id
                ELSE f.requester_id
            END
    
        FROM friendships f
    
        WHERE (
            f.requester_id = :userId
            OR
            f.receiver_id = :userId
        )
    
        AND f.status = :status
    )
    
    
    AND ST_DWithin(
        p.location,
        ST_SetSRID(
            ST_Point(:longitude,:latitude),
            4326
        )::geography,
        100
    )
    
    """, nativeQuery = true)
    List<Post> findFriendPostsWithinDistance(
            @Param("userId") UUID userId,
            @Param("status") String status,
            @Param("longitude") Double longitude,
            @Param("latitude") Double latitude
    );

    @Query(value = """
    SELECT EXISTS (
    
        SELECT 1
        FROM posts candidate
    
        WHERE candidate.id IN (:candidateIds)
    
        -- chưa được reup
        AND NOT EXISTS (
            SELECT 1
            FROM profile_reup_posts rp
            WHERE rp.profile_id = :profileId
            AND rp.post_id = candidate.id
        )
    
    
        -- không có post reup nào nằm trong 1000m
        AND NOT EXISTS (
    
            SELECT 1
            FROM profile_reup_posts rp2
    
            JOIN posts old_post
            ON old_post.id = rp2.post_id
    
            WHERE rp2.profile_id = :profileId
    
            AND ST_DWithin(
                candidate.location,
                old_post.location,
                1000
            )
        )
    
    )
    """, nativeQuery = true)
    boolean existsUnlockablePost(
            @Param("profileId") UUID profileId,
            @Param("candidateIds") List<UUID> candidateIds
    );
}
