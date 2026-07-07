package com.example.lockly.domain.entity.main;

import com.example.lockly.domain.entity.main.enumEntity.Emoji;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "emoji_posts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_sender",
                        columnNames = {"post_id", "sender_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmojiPost {

    @Id
    @Column(columnDefinition = "BINARY(16)", name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column
    private Emoji emoji;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        createdAt = LocalDateTime.now();
    }
}