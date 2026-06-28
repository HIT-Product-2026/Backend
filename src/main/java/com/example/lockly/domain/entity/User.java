package com.example.lockly.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
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
    private UUID id;


    @Column(length = 100, nullable = false, unique = true)
    private String username;

    @Column(length = 100, name = "display_name", nullable = false)
    private String displayName;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 100, nullable = false)
    private String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column
    private UserMode mode;

    // Địa chỉ của thiết bị, giúp fe biết cần gửi thông báo đến đâu
    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        if (id == null){
            id = UuidCreator.getTimeOrderedEpoch();
        }
        if (displayName == null){
            displayName = email.substring(0, email.indexOf("@"));
        }
        createdAt = LocalDateTime.now();
    }
}
