package com.example.lockly.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.Date;
=======
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvalidatedToken {

    @Id
    String id;

    LocalDateTime expiryTime;
<<<<<<< HEAD
}
=======
}
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
