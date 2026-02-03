package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_seeker_profiles")
@Getter
@Setter
public class JobSeekerProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // 집 주소
    @Column(name = "home_address", columnDefinition = "TEXT")
    private String homeAddress;

    // 경력
    @Column(name = "career", columnDefinition = "TEXT")
    private String career;

    // 한 줄 소개
    @Column(name = "bio", length = 150)
    private String bio;

    // 위도
    @Column(name = "lat", nullable = false)
    private Double lat;

    // 경도
    @Column(name = "lng", nullable = false)
    private Double lng;

    // 주
    @Column(name = "state", columnDefinition = "TEXT")
    private String state;

    // 도시
    @Column(name = "city", columnDefinition = "TEXT")
    private String city;
}

