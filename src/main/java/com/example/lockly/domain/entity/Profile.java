package com.example.lockly.domain.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

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

    // ========== Danh sách nhiệm vụ ==========

    // Danh sách profile mà người này từng ghé thăm
    @ElementCollection
    @CollectionTable(
            name = "profile_visited_profiles",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "visited_profile_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private Set<UUID> visitedProfileIds = new HashSet<>();

    // Danh sách người từng ghé thăm profile này
    @ElementCollection
    @CollectionTable(
            name = "profile_visitors",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "visitor_profile_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private Set<UUID> visitorProfileIds = new HashSet<>();

    // Danh sách bạn bè từng chụp chung
    @ElementCollection
    @CollectionTable(
            name = "profile_photographed_friends",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "friend_user_id", columnDefinition = "BINARY(16)")
    @Builder.Default
    private Set<UUID> photographedFriendIds = new HashSet<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "profile_cities",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "city")
    @Builder.Default
    private Set<String> cityCodes = new HashSet<>();

    // ======= Flag ========
    @Column(name = "visited_profiles_completed")
    @Builder.Default
    private boolean visitedProfilesCompleted = false;

    @Column(name = "visitors_completed")
    @Builder.Default
    private boolean visitorsCompleted = false;

    @Column(name = "photographed_friends_completed")
    @Builder.Default
    private boolean photographedFriendsCompleted = false;

    @Column(name = "cities_completed")
    @Builder.Default
    private boolean citiesCompleted = false;


    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}