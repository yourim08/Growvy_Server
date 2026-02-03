package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "employer_profiles")
@Getter
@Setter
public class EmployerProfile {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // 회사명
    @Column(name = "company_name", length = 100)
    private String companyName;

    // 회사 주소
    @Column(name = "business_address", columnDefinition = "TEXT")
    private String businessAddress;

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

