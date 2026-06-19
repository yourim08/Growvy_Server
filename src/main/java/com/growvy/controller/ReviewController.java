package com.growvy.controller;

import com.growvy.annotation.CurrentUser;
import com.growvy.dto.req.ReviewUpdateRequest;
import com.growvy.dto.res.ReceivedReviewResponse;
import com.growvy.dto.res.WrittenReviewResponse;
import com.growvy.entity.User;
import com.growvy.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 로그인한 사용자가 받은 리뷰 목록 조회
     */
    @Operation(summary = "[MyPage] 받은 리뷰 조회", description = "로그인한 유저가 받은 리뷰 목록")
    @GetMapping("/received")
    public List<ReceivedReviewResponse> getMyReceivedReviews(
            @CurrentUser User user // 기존 스타일 유지
    ) {
        return reviewService.getReceivedReviews(user.getId());
    }

    /**
     * 로그인한 사용자가 작성한 리뷰 목록 조회
     */
    @Operation(summary = "[MyPage] 작성한 리뷰 조회", description = "로그인한 유저가 쓴 리뷰 목록")
    @GetMapping("/written")
    public List<WrittenReviewResponse> getMyWrittenReviews(
            @CurrentUser User user // 기존 스타일 유지
    ) {
        return reviewService.getWrittenReviews(user.getId());
    }

    @Operation(summary = "[MyPage] 작성한 리뷰 수정", description = "내가 쓴 리뷰의 별점과 내용을 수정합니다.")
    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @CurrentUser User user,
            @RequestBody ReviewUpdateRequest req
    ) {
        reviewService.updateReview(reviewId, user, req);
        return ResponseEntity.ok().build(); // 200 OK 반환
    }
}