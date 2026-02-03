package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "records")
@Getter
@Setter
public class JobRecord {

    @Id
    @Column(name = "application_id")
    private Long applicationId; // 한 신청자당 한 기록

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_completed")
    private Boolean isCompleted; // 임시저장 여부

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "company_name", columnDefinition = "TEXT")
    private String companyName;

    @Column(name = "post_title", columnDefinition = "TEXT")
    private String postTitle;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Record ↔ RecordImage 1:N 관계
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<JobRecordImage> recordImages = new ArrayList<>();
}
