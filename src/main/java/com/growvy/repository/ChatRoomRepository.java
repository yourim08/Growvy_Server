package com.growvy.repository;

import com.growvy.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// ChatRoom 레포
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 특정 구직자, 고용자, jobPost 조합으로 방 조회
    ChatRoom findByJobSeekerIdAndEmployerIdAndJobPostId(Long jobSeekerId, Long employerId, Long jobPostId);

    // 구직자가 참여한 모든 채팅방 조회
    List<ChatRoom> findAllByJobSeekerId(Long jobSeekerId);

    // 고용자가 참여한 모든 채팅방 조회
    List<ChatRoom> findAllByEmployerId(Long employerId);
}