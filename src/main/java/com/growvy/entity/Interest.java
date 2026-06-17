package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "interests")
@Getter
@Setter
public class Interest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관심사 이름 (청소, 서빙, 물류 등 등)
    @Column(name = "name", columnDefinition = "TEXT")
    private String name;


    // 관심사 유형 (INDUSTRY, SURVEY, EMPLOYMENT)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType type;

    public enum InterestType {
        INDUSTRY,
        EMPLOYMENT,
        ENERGY_STYLE,
        WORK_ENVIRONMENT,
        SOCIAL_PREFERENCE,
        COMFORT_ZONE,
        MAIN_GOAL,
        WORK_PACE
    }
}

