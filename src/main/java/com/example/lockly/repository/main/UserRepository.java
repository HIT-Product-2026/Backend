package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail (String email);
    boolean existsByUsername (String username);
    Optional<User> findUserDetailByUsername(String username);
    Optional<User> findByEmail(String email);
    Page<User> findByUsernameContaining(String username, Pageable page);
    Page<User> findByEmailContaining(String email, Pageable page);
}
