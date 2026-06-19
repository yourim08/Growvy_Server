package com.growvy.service;

import com.growvy.dto.req.ReviewUpdateRequest;
import com.growvy.dto.res.ReceivedReviewResponse;
import com.growvy.dto.res.WrittenReviewResponse;
import com.growvy.entity.Review;
import com.growvy.entity.User;
import com.growvy.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // [공통] 내가 받은 리뷰 조회
    @Transactional
    public List<ReceivedReviewResponse> getReceivedReviews(Long targetUserId) {
        List<Review> reviews = reviewRepository.findByTargetUserId(targetUserId);

        return reviews.stream()
                .map(review -> ReceivedReviewResponse.builder()
                        .reviewId(review.getId()) // 🌟 ID 매핑 추가
                        .title(review.getJobPost().getTitle())
                        .rating(review.getRating())
                        .writer(review.getReviewer().getName())
                        .body(review.getComment())
                        .build())
                .toList();
    }

    // [공통] 내가 쓴 리뷰 조회
    @Transactional
    public List<WrittenReviewResponse> getWrittenReviews(Long reviewerId) {
        // 작성자 ID로 리뷰 목록을 조회합니다.
        List<Review> reviews = reviewRepository.findByReviewerId(reviewerId);

        return reviews.stream()
                .map(review -> WrittenReviewResponse.builder()
                        .reviewId(review.getId())
                        .title(review.getJobPost().getTitle())
                        .rating(review.getRating())
                        // 내가 쓴 리뷰이므로 '작성자(나)' 대신 '대상자(Target)'의 이름을 세팅합니다.
                        .targetName(review.getTargetUser().getName())
                        .body(review.getComment())
                        .build())
                .toList();
    }

    // 🌟 [추가] 내가 쓴 별점 & 리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, User user, ReviewUpdateRequest req) {
        // 1. 수정할 리뷰가 존재하는지 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 2. [보안 검증] 현재 로그인한 유저가 이 리뷰를 작성한 주인이 맞는지 확인
        if (!review.getReviewer().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 리뷰를 수정할 권한이 없습니다.");
        }

        // 3. 리뷰 내용 및 별점 수정 (더티 체킹에 의해 메서드가 끝나면 DB에 반영됨)
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        updateAverageRating(review.getTargetUser());

    }

    // 별점 업데이트 전용 private 메서드
    private void updateAverageRating(User targetUser) {
        Double average = reviewRepository.calculateAverageRatingByTargetUserId(targetUser.getId());

        // Double 타입에 맞춰 그대로 세팅 (만약 리뷰가 아예 없다면 초기값인 0.0 처리)
        Double newRating = (average != null) ? average : 0.0;

        targetUser.setAverageRating(newRating);
    }
}
