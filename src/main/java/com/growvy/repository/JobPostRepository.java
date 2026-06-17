package com.growvy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.growvy.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    @Query("""
            SELECT j FROM JobPost j
            WHERE j.user.id = :employerId
            AND NOT EXISTS (
                SELECT m FROM JobPostMember m
                WHERE m.jobPost = j
            )
            ORDER BY j.createdAt DESC
            """)
    List<JobPost> findHiringPosts(@Param("employerId") Long employerId);
    // JobPostRepository 등에 있는 '구직자 지원 공고 조회' 쿼리
    @Query(value = """
    SELECT jp.* FROM job_posts jp
    JOIN applications a ON jp.id = a.job_post_id
    WHERE a.job_seeker_id = :userId
    ORDER BY a.applied_at DESC  -- 🌟 생성일이 아니라 '지원일' 기준 정렬
""",
            countQuery = """
    SELECT COUNT(*) FROM applications a 
    WHERE a.job_seeker_id = :userId
""",
            nativeQuery = true)
    Page<JobPost> findAppliedPostsByApplicationDate(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT DISTINCT j FROM JobPost j
            JOIN JobPostMember m ON m.jobPost = j
            WHERE j.user.id = :employerId
            AND m.status = 'WORKING'
            ORDER BY j.createdAt DESC
            """)
    List<JobPost> findOngoingPosts(@Param("employerId") Long employerId);

    @Query("""
            SELECT j FROM JobPost j
            WHERE j.user.id = :employerId
            AND EXISTS (
                SELECT 1 FROM JobPostMember m
                WHERE m.jobPost = j
                GROUP BY m.jobPost
                HAVING COUNT(m) > 0
                   AND SUM(CASE WHEN m.status = 'WORKING' THEN 1 ELSE 0 END) = 0
            )
            ORDER BY j.createdAt DESC
            """)
    List<JobPost> findDonePosts(@Param("employerId") Long employerId);

    // [구직자 전용] 사용자 성향 맞춤형 공고 조회 (가중치 기반 매칭 점수순)
    @Query(value = """
        SELECT jp.* FROM job_posts jp
        LEFT JOIN (
            SELECT jpt.job_post_id, SUM(ir.weight) as match_score
            FROM job_seeker_interests jsi
            JOIN interest_recommendations ir ON jsi.interest_id = ir.source_interest_id
            JOIN job_post_tags jpt ON ir.target_interest_id = jpt.interest_id
            WHERE jsi.job_seeker_id = :userId
            GROUP BY jpt.job_post_id
        ) score_table ON jp.id = score_table.job_post_id
        WHERE 
            -- 자신이 작성/소속된 공고 제외
            NOT EXISTS (SELECT 1 FROM job_post_members jpm WHERE jpm.job_post_id = jp.id)
            -- 이미 지원한 공고 제외 (a.user_id -> a.job_seeker_id 로 수정 완료!)
            AND NOT EXISTS (SELECT 1 FROM applications a WHERE a.job_post_id = jp.id AND a.job_seeker_id = :userId)
        -- 1순위: 매칭 점수 높은 순, 2순위: 최신순
        ORDER BY COALESCE(score_table.match_score, 0) DESC, jp.created_at DESC
    """,
            countQuery = """
        SELECT COUNT(*) 
        FROM job_posts jp
        WHERE 
            NOT EXISTS (SELECT 1 FROM job_post_members jpm WHERE jpm.job_post_id = jp.id)
            -- countQuery 부분도 동일하게 수정 완료!
            AND NOT EXISTS (SELECT 1 FROM applications a WHERE a.job_post_id = jp.id AND a.job_seeker_id = :userId)
    """,
            nativeQuery = true)
    Page<JobPost> findRecommendedPostsForJobSeeker(@Param("userId") Long userId, Pageable pageable);


    // [구직자 전용] 인기순(조회수 기준) 공고 조회 - (이전에 만드신 것도 똑같이 수정해야 합니다)
    @Query(value = """
        SELECT jp.* FROM job_posts jp
        WHERE 
            NOT EXISTS (SELECT 1 FROM job_post_members jpm WHERE jpm.job_post_id = jp.id)
            AND NOT EXISTS (SELECT 1 FROM applications a WHERE a.job_post_id = jp.id AND a.job_seeker_id = :userId)
        ORDER BY jp.view DESC, jp.created_at DESC
    """,
            countQuery = """
        SELECT COUNT(*) FROM job_posts jp
        WHERE 
            NOT EXISTS (SELECT 1 FROM job_post_members jpm WHERE jpm.job_post_id = jp.id)
            AND NOT EXISTS (SELECT 1 FROM applications a WHERE a.job_post_id = jp.id AND a.job_seeker_id = :userId)
    """,
            nativeQuery = true)
    Page<JobPost> findPopularPostsForJobSeeker(@Param("userId") Long userId, Pageable pageable);


    // 검색어(keyword)가 제목에 포함되면서, 내가 지원한 공고는 제외하고 최신순으로 조회
    @Query("""
        SELECT p FROM JobPost p 
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
          AND p.id NOT IN (
              SELECT a.jobPost.id 
              FROM Application a 
              WHERE a.jobSeeker.userId = :userId
          )
        ORDER BY p.createdAt DESC
    """)
    List<JobPost> searchJobPostsExcludingApplied(
            @Param("keyword") String keyword,
            @Param("userId") Long userId
    );
}
