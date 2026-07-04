package com.example.lockly.repository.main;

import com.example.lockly.domain.entity.main.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    boolean existsById(String id);

}
