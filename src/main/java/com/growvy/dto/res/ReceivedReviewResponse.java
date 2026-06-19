package com.growvy.dto.res;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class ReceivedReviewResponse {
    private Long reviewId; // 🌟 추가됨 (나중을 위한 확장성 대비)
    private String title;
    private int rating;
    private String writer;
    private String body;
}