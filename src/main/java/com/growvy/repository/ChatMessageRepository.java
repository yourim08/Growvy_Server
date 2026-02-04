package com.growvy.repository;
import com.growvy.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// ChatMessage 레포
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 방별 메시지 조회 (시간 순)
    List<ChatMessage> findAllByRoomIdOrderByCreatedAtAsc(Long roomId);
    
    // 특정 방, 특정 보낸 사람 메시지 조회
    List<ChatMessage> findAllByRoomIdAndSenderId(Long roomId, Long senderId);
}