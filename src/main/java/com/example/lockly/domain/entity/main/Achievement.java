package com.example.lockly.domain.entity.main;

import com.example.lockly.domain.entity.main.enumEntity.AchievementName;
import com.example.lockly.domain.entity.main.enumEntity.AchievementType;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "achievements",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"profile_id", "achievement_name"})
        },

        indexes = {
        @Index(name = "idx_profile", columnList = "profile_id")
            }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_name", nullable = false, length = 50)
    private AchievementName achievementName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private AchievementType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}