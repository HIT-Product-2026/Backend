package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.User;
import com.example.lockly.domain.entity.main.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM UserBlock b
        WHERE (b.blocker = :user1 AND b.blocked = :user2)
           OR (b.blocker = :user2 AND b.blocked = :user1)
    """)
    boolean existsBlockBetweenUsers(
            @Param("user1") User user1,
            @Param("user2") User user2
    );

    @Query("""
        SELECT b
        FROM UserBlock b
        JOIN FETCH b.blocked
        WHERE b.blocker = :blocker
        ORDER BY b.createdAt DESC
    """)
    List<UserBlock> findByBlockerWithBlocked(@Param("blocker") User blocker);
}