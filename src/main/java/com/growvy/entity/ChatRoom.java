package com.growvy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"job_post_id", "job_seeker_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 공고의 채팅방인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    // 구직자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private JobSeekerProfile jobSeeker;

    // 구인자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(
            mappedBy = "room",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ChatMessage> messages = new ArrayList<>();
}