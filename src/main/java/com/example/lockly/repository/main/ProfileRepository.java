package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.Profile;
import com.example.lockly.domain.entity.main.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(UUID userId);
    Optional<Profile> findByUser(User user);
}
