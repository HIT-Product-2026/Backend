package com.example.lockly.repository;

import com.example.lockly.domain.entity.Achievement;
import com.example.lockly.domain.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    void deleteByProfile(Profile profile);
}
