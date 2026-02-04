package com.growvy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 구인글과 관련된 채팅인지
    @Column(name = "job_post_id", nullable = false)
    private Long jobPostId;

    // 참여자
    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;

    @Column(name = "employer_id", nullable = false)
    private Long employerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 메시지 리스트 (양방향)
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;
}