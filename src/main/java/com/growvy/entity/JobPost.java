package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_posts")
@Getter
@Setter
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 구인자 (employer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private User user;

    // 제목
    @Column(name = "title", length = 100)
    private String title;

    // 상호명
    @Column(name = "company_name", columnDefinition = "TEXT")
    private String companyName;

    // 소개
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 모집 인원
    @Column(name = "count")
    private int count;

    // 시작일
    @Column(name = "start_date", updatable = false)
    private LocalDate startDate;

    // 종료일
    @Column(name = "end_date", updatable = false)
    private LocalDate endDate;

    // 시작 시간
    @Column(name = "start_time", columnDefinition = "TEXT")
    private String startTime;

    // 종료 시간
    @Column(name = "end_time", columnDefinition = "TEXT")
    private String endTime;

    // 시급
    @Column(name = "hourly_wage")
    private int hourlyWage;

    // 주소
    @Column(name = "job_address", columnDefinition = "TEXT")
    private String jobAddress;

    // 위도 / 경도
    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    // 생성일
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 공고 상태
    @Enumerated(EnumType.STRING)
    private Status status;

    // 조회수
    @Column(name = "view")
    private Long view = 0L;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobPostTag> jobPostTags = new ArrayList<>();

    @OneToMany(mappedBy = "jobPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")  // sortOrder 기준으로 정렬
    private List<JobPostImage> jobPostImages = new ArrayList<>();

    public enum Status {
        OPEN, CLOSED, DONE
    }
}