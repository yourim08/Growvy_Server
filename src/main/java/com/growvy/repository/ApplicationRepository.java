package com.growvy.repository;

import com.growvy.entity.Application;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobSeekerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobPostAndJobSeeker(
            JobPost jobPost,
            JobSeekerProfile jobSeeker
    );

    List<Application> findByJobSeeker_UserIdAndStatus(
            Long userId,
            Application.Status status
    );

    // JobPostRepository 등에 있는 '구직자 지원 공고 조회' 쿼리
// 내가 지원한(status) 공고를 지원일(appliedAt) 최신순으로 조회
    @Query("""
        SELECT a FROM Application a
        JOIN FETCH a.jobPost jp 
        WHERE a.jobSeeker.userId = :userId
          AND a.status = :status
        ORDER BY a.appliedAt DESC
    """)
    List<Application> findAppliedPostsByApplicationDate(
            @Param("userId") Long userId,
            @Param("status") Application.Status status
    );

    // (추천) 특정 공고의 지원자 목록을 조회할 때 구직자와 유저 정보까지 한 번에(Fetch Join) 가져오기
    @Query("""
        SELECT a FROM Application a
        JOIN FETCH a.jobSeeker js
        JOIN FETCH js.user u
        WHERE a.jobPost.id = :postId
          AND a.status = :status
    """)
    List<Application> findByJobPost_IdAndStatus(
            @Param("postId") Long postId,
            @Param("status") Application.Status status
    );


    int countByJobPost_Id(Long jobPostId);

    // 각 공고 ID별로 지원자 수를 GROUP BY해서 한 번에 조회
    @Query("SELECT a.jobPost.id, COUNT(a) FROM Application a " +
            "WHERE a.jobPost.id IN :postIds " +
            "GROUP BY a.jobPost.id")
    List<Object[]> countApplicantsByPostIds(@Param("postIds") List<Long> postIds);
}