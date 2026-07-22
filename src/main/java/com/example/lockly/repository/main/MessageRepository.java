package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("""
    SELECT m
    FROM Message m
    JOIN FETCH m.sender
    WHERE m.conversation.id = :conversationId
    ORDER BY m.createdAt ASC
""")
    List<Message> findByConversationId(
            @Param("conversationId")
            UUID conversationId
    );


    @Query("""
        SELECT m
        FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation.id = :conversationId
        ORDER BY m.createdAt ASC, m.id ASC
        """)
    Slice<Message> findByConversationFirstPage(
            @Param("conversationId") UUID conversationId,
            Pageable pageable
    );

    @Query("""
        SELECT m
        FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation.id = :conversationId
          AND (
                m.createdAt > :cursorCreatedAt
                OR (
                    m.createdAt = :cursorCreatedAt
                    AND m.id > :cursorId
                )
          )
        ORDER BY m.createdAt ASC, m.id ASC
        """)
    Slice<Message> findByConversationNextPage(
            @Param("conversationId") UUID conversationId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );
}
