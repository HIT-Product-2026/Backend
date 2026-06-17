package com.example.lockly.repository;

import com.example.lockly.domain.entity.FriendshipStatus;
import com.example.lockly.domain.entity.Friendships;
import com.example.lockly.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipsRepository extends JpaRepository<Friendships, String> {
    List<Friendships> findByRequesterAndStatus(User requester, FriendshipStatus status);
    List<Friendships> findByReceiverAndStatus(User receiver, FriendshipStatus status);
    Optional<Friendships> findById(String id);
}
