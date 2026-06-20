package com.example.lockly.repository;

import com.example.lockly.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

=======
import org.springframework.stereotype.Repository;
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail (String email);
    boolean existsByUsername (String email);
    Optional<User> findByUsername(String username);
    Optional<User> findUserDetailByUsername(String username);
<<<<<<< HEAD
=======
    Optional<User> findByEmail(String email);
>>>>>>> acba5e954f2a3c3fb5981d13d96bf84354d83cbe
}
