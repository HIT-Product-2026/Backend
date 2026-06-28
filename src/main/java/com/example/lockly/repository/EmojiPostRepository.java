package com.example.lockly.repository;

import com.example.lockly.domain.entity.EmojiPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmojiPostRepository extends JpaRepository<EmojiPost, UUID> {
}
