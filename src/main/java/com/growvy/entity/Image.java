package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "images")
@Getter
@Setter
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제목
    private String title;

    // URL
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // 정렬 순서
    @Column(name = "sort_order")
    private Integer sortOrder;

}
