package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "job_post_images")
@Getter
@Setter
public class JobPostImage {
    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // job_post ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    // 경로
    @Column(name = "image_url", nullable = false)
    private String imageUrl;


    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}