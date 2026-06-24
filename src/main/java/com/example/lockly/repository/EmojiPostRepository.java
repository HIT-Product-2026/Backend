package com.example.lockly.repository;

import com.example.lockly.domain.entity.EmojiPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmojiPostRepository extends JpaRepository<EmojiPost, String> {
}
