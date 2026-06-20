package com.example.lockly.repository;

import com.example.lockly.domain.entity.Conversation;
import com.example.lockly.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    @Query("""
    select c
    from Conversation c
    join fetch c.user1
    join fetch c.user2
    where c.user1 = :user
       or c.user2 = :user
    order by c.lastMessageTime desc
""")
    List<Conversation> findByUser(@Param("user") User user);

    boolean existsByUser1AndUser2(User user1, User user2);

    Optional<Conversation> findById(String id);
}
