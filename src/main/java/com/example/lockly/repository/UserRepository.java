package com.example.lockly.repository;

import com.example.lockly.domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail (String email);
    boolean existsByUsername (String email);
    Optional<User> findByUsername(String username);
    Optional<User> findUserDetailByUsername(String username);
    Optional<User> findByEmail(String email);
}
