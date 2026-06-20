package com.example.lockly.repository;

import com.example.lockly.domain.entity.Post;
import com.example.lockly.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostsRepository extends JpaRepository<Post, String> {
    List<Post> findByUser(User user);
}
