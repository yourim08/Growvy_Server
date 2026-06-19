package com.growvy.repository;

import com.growvy.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 특정 공고와 특정 구직자의 채팅방이 이미 존재하는지 확인
    Optional<ChatRoom> findByJobPost_IdAndJobSeeker_UserId(Long jobPostId, Long userId);

    // 유저가 구인자(employer)이거나 구직자(jobSeeker)로 속해 있는 모든 채팅방 조회
    @Query("SELECT r FROM ChatRoom r WHERE r.employer.id = :userId OR r.jobSeeker.userId = :userId ORDER BY r.createdAt DESC")
    List<ChatRoom> findMyChatRooms(@Param("userId") Long userId);

}