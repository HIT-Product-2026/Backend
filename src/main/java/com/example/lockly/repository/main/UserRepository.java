package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail (String email);
    boolean existsByUsername (String username);
    Optional<User> findUserDetailByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u
        FROM User u
        WHERE
        (
            LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND u.id <> :currentUserId
        AND NOT EXISTS (
            SELECT 1
            FROM Friendship f
            WHERE
                f.status = com.example.lockly.domain.entity.main.enumEntity.FriendshipStatus.ACCEPTED
                AND (
                    (f.requester.id = :currentUserId AND f.receiver.id = u.id)
                    OR
                    (f.receiver.id = :currentUserId AND f.requester.id = u.id)
                )
        )
        """)
    Page<User> searchStrangers(
            UUID currentUserId,
            String keyword,
            Pageable pageable
    );

    Optional<User> findByUsername(String username);
}
