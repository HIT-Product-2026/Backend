package com.example.lockly.repository;

import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendship;
import com.example.lockly.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipsRepository extends JpaRepository<Friendship, UUID   > {
    @Query("""
    SELECT f
    FROM Friendship f
    JOIN FETCH f.receiver
    WHERE f.requester = :requester
      AND f.status = :status
""")
    List<Friendship> findByRequesterAndStatus(
            @Param("requester") User requester,
            @Param("status") FriendshipStatus status
    );
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);
    Optional<Friendship> findById(UUID id);
    boolean existsByRequesterAndReceiver(User Requester, User Receiver);
    boolean existsByReceiverAndRequester(User receiver, User requester);

    @Query("""
    SELECT 
        CASE
            WHEN f.requester.id = :userId THEN f.receiver.id
            ELSE f.requester.id
        END
    FROM Friendship f
    WHERE (f.requester.id = :userId OR f.receiver.id = :userId)
    AND f.status = com.example.lockly.domain.entity.FriendshipStatus.ACCEPTED
""")
    List<UUID> findFriendIds(@Param("userId") UUID userId);
}
