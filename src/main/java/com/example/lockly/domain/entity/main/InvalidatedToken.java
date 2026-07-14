package com.example.lockly.domain.entity.main;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invalidated_token")
public class InvalidatedToken {

    @Id
    String id;

    LocalDateTime expiryTime;
}