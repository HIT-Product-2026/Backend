package com.example.lockly.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(columnDefinition = "TEXT")
    private String hobbies;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "post_count")
    @Builder.Default
    private Integer postCount = 0;

    @Column(name = "current_post_streak")
    @Builder.Default
    private Integer currentPostStreak = 0;

    @Column(name = "longest_post_streak")
    @Builder.Default
    private Integer longestPostStreak = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_post_id")
    private Post latestPost;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_post_id")
    private Post favoritePost;

    // Danh sách profile mà người này từng ghé thăm
    @ElementCollection
    @CollectionTable(
            name = "profile_visited_profiles",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "visited_profile_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private List<UUID> visitedProfileIds = new ArrayList<>();

    // Danh sách người từng ghé thăm profile này
    @ElementCollection
    @CollectionTable(
            name = "profile_visitors",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "visitor_profile_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private List<UUID> visitorProfileIds = new ArrayList<>();

    // Danh sách bạn bè từng chụp chung
    @ElementCollection
    @CollectionTable(
            name = "profile_photographed_friends",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "friend_user_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private List<UUID> photographedFriendIds = new ArrayList<>();


    // Embedding khuôn mặt
    @Lob
    @Column(name = "face_embedding")
    private byte[] faceEmbedding;

    // Tổng số lần chụp với tất cả bạn bè
    @Column(name = "total_photo_with_friends")
    @Builder.Default
    private Integer totalPhotoWithFriends = 0;


    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}