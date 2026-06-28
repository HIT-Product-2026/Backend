package com.example.lockly.repository;

import com.example.lockly.domain.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.UUID;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

    boolean existsById(String id);

}
