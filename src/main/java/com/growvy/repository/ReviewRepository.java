package com.growvy.repository;

import com.growvy.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTargetUserId(Long targetUserId);
    List<Review> findByReviewerId(Long reviewerId);
    // 내가 특정 공고에서 남긴 리뷰 목록 조회
    List<Review> findByJobPost_IdAndReviewer_Id(Long postId, Long reviewerId);

    boolean existsByJobPost_IdAndReviewer_IdAndTargetUser_Id(
            Long postId, Long reviewerId, Long targetUserId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetUser.id = :targetUserId")
    Double calculateAverageRatingByTargetUserId(@Param("targetUserId") Long targetUserId);

    // [최적화 🌟] 내가(구인자가) 특정 공고들에 대해 작성한 리뷰 개수를 한방에 GROUP BY 조회
    @Query("""
            SELECT r.jobPost.id, COUNT(r) 
            FROM Review r 
            WHERE r.jobPost.id IN :postIds 
            AND r.reviewer.id = :reviewerId 
            GROUP BY r.jobPost.id
            """)
    List<Object[]> countWrittenReviewsByPostIds(@Param("postIds") List<Long> postIds, @Param("reviewerId") Long reviewerId);
}