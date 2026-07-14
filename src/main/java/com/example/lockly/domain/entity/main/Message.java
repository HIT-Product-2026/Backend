package com.example.lockly.domain.entity.main;

import com.example.lockly.domain.entity.main.enumEntity.MessageType;
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
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // Nếu là ảnh thì sẽ chứa đường dẫn ở đây
    private String content;

    @Enumerated(EnumType.STRING)
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
