package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.Achievement;
import com.example.lockly.domain.entity.main.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    void deleteByProfile(Profile profile);
}
