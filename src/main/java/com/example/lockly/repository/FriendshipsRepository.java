package com.example.lockly.repository;

import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendship;
import com.example.lockly.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipsRepository extends JpaRepository<Friendship, String> {
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
    Optional<Friendship> findById(String id);
    boolean existsByRequesterAndReceiver(User Requester, User Receiver);
    boolean existsByReceiverAndRequester(User receiver, User requester);
}
