package com.example.lockly.repository;

import com.example.lockly.domain.entity.Conversation;
import com.example.lockly.domain.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, String> {
    @Query("""
    SELECT m
    FROM Message m
    JOIN FETCH m.sender
    WHERE m.conversation.id = :conversationId
    ORDER BY m.createdAt ASC
""")
    List<Message> findByConversationId(
            @Param("conversationId")
            String conversationId
    );

    Optional<Message> findById(String id);
}
