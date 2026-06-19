package com.growvy.repository;

import com.growvy.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoom_IdOrderByCreatedAtAsc(Long roomId);

    Optional<ChatMessage> findTopByRoom_IdOrderByCreatedAtDesc(Long roomId);
}