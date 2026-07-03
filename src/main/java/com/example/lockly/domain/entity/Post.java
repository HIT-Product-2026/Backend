package com.example.lockly.domain.entity;

import com.example.lockly.domain.entity.enumEntity.NsfwStatus;
import com.example.lockly.domain.entity.enumEntity.PostModeLocation;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String bucket;

    @Column(name = "object_name")
    private String objectName;

    @Column(length = 100)
    private String caption;

    @Column(name = "mode_location")
    private PostModeLocation modeLocation;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private NsfwStatus nsfw;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        if (id == null){
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (nsfw == null){
            nsfw = NsfwStatus.PROCESSING;
        }
        createdAt = LocalDateTime.now();
    }
}
