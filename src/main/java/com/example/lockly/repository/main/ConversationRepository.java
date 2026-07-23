package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.Conversation;
import com.example.lockly.domain.entity.main.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("""
    select c
    from Conversation c
    join fetch c.user1
    join fetch c.user2
    where c.user1 = :user
       or c.user2 = :user
    order by c.lastMessageTime desc
    """)
    List<Conversation> findByUser(@Param("user") User user);

    boolean existsByUser1AndUser2(User user1, User user2);

    @Query("""
    select c
    from Conversation c
    where (c.user1 = :user1 and c.user2 = :user2)
       or (c.user1 = :user2 and c.user2 = :user1)
    """)
    Optional<Conversation> findByUsers(
            @Param("user1") User user1,
            @Param("user2") User user2
    );

    @Query("""
    SELECT c
    FROM Conversation c
    JOIN FETCH c.user1
    JOIN FETCH c.user2
    WHERE c.user1.id = :userId
       OR c.user2.id = :userId
    ORDER BY c.lastMessageTime DESC, c.id DESC
    """)
    Slice<Conversation> findByUserFirstPage(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    @Query("""
    SELECT c
    FROM Conversation c
    JOIN FETCH c.user1
    JOIN FETCH c.user2
    WHERE (
            c.user1.id = :userId
            OR c.user2.id = :userId
          )
      AND (
            c.lastMessageTime < :cursorLastMessageTime
            OR (
                c.lastMessageTime = :cursorLastMessageTime
                AND c.id < :cursorId
            )
          )
    ORDER BY c.lastMessageTime DESC, c.id DESC
    """)
    Slice<Conversation> findByUserNextPage(
            @Param("userId") UUID userId,
            @Param("cursorLastMessageTime") LocalDateTime cursorLastMessageTime,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    @Query("""
    select c
    from Conversation c
    join fetch c.user1 u1
    join fetch c.user2 u2
    left join fetch c.lastMessage lm
    where c.id = :id
""")
    Optional<Conversation> findByIdWithUsersAndLastMessage(@Param("id") UUID id);
}
