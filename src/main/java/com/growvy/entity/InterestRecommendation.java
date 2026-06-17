package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "interest_recommendations",
        indexes = {
                @Index(
                        name = "idx_interest_recommendation_source",
                        columnList = "source_interest_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_source_target_interest",
                        columnNames = {
                                "source_interest_id",
                                "target_interest_id"
                        }
                )
        }
)
@Getter
@Setter
public class InterestRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 선택한 관심사(설문 응답)
    @ManyToOne
    @JoinColumn(name = "source_interest_id", nullable = false)
    private Interest sourceInterest;

    // 추천 대상(Industry, Employment)
    @ManyToOne
    @JoinColumn(name = "target_interest_id", nullable = false)
    private Interest targetInterest;

    // 추천 가중치
    @Column(nullable = false)
    private Integer weight;
}