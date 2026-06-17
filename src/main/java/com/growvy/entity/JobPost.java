package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private Long id;

    // 구인자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private User user;

    // 공고명
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    // 회사명
    @Column(name = "company_name", columnDefinition = "TEXT")
    private String companyName;

    // 주소
    @Column(name = "job_address", columnDefinition = "TEXT")
    private String jobAddress;

    // 담당 업무
    @Column(name = "responsibility", columnDefinition = "TEXT")
    private String responsibility;

    // 공고 정보
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 시작일
    @Column(name = "start_date")
    private LocalDateTime startDate;

    // 종료일
    @Column(name = "end_date")
    private LocalDateTime endDate;

    // 모집 인원
    @Column(name = "count")
    private int count;

    // 시급 (int -> double 수정)
    @Column(name = "hourly_rates")
    private double hourlyRates;

    // 할증 수당 (int -> double 수정)
    @Column(name = "penalty_rates")
    private double penaltyRates;

    // 연금 지급 방식
    @Enumerated(EnumType.STRING)
    @Column(name = "superannuation")
    private Superannuation superannuation;

    // 모집 마감일
    @Column(name = "recruitment_deadline")
    private LocalDateTime recruitmentDeadline;

    // 생성일
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 위도
    @Column(name = "lat")
    private Double lat;

    // 경도
    @Column(name = "lng")
    private Double lng;

    // 주
    @Column(name = "state", columnDefinition = "TEXT")
    private String state;

    // 도시
    @Column(name = "city", columnDefinition = "TEXT")
    private String city;

    // 조회수
    @Column(name = "view")
    private Long view = 0L;

    @OneToMany(
            mappedBy = "jobPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder ASC")
    private List<JobPostImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "jobPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<JobPostTag> jobPostTags = new ArrayList<>();

    @OneToMany(
            mappedBy = "jobPost",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<JobPostSchedule> schedules = new ArrayList<>();

    public enum Superannuation {
        PAID_SEPARATELY,
        INCLUDED_IN_RATE
    }
}