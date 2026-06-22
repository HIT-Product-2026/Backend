package com.example.lockly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String bucket;

    @Column(name = "object_name")
    private String objectName;

    @Column(length = 100)
    private String caption;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "mode_location")
    private PostModeLocation modeLocation;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
