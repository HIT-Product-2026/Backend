package com.example.lockly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id;

    @Column(length = 100, nullable = false)
    private String username;

    @Column(length = 100, name = "display_name", nullable = false)
    private String displayName;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @PrePersist
    void prePersist(){
        if (id == null){
            id = UUID.randomUUID().toString();
        }
        createAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate(){
        lastActiveAt = LocalDateTime.now();
    }
}
