package com.growvy.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewUpdateRequest {
    private int rating;     // 수정할 별점
    private String comment; // 수정할 리뷰 내용
}