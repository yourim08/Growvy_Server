package com.growvy.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WrittenReviewResponse {
    private Long reviewId;
    private String title;
    private int rating;
    private String targetName; // 🌟 리뷰를 받은 사람의 이름
    private String body;
}
