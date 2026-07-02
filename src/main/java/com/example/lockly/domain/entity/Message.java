package com.example.lockly.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_text")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @Column(columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // Nếu là ảnh thì sẽ chứa đường dẫn ở đây
    private String content;

    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        if (id == null){
            id = UuidCreator.getTimeOrderedEpoch();
        }
        createdAt = LocalDateTime.now();
    }
}
