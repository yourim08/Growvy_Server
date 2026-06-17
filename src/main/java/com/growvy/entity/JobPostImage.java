package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_post_images")
@Getter
@Setter
public class JobPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공고
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    // 이미지 경로
    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    // 표시 순서
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}