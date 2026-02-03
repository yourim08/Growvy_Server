package com.growvy.repository;

import com.growvy.entity.Application;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // 구직자가 신청한 모든 신청 조회
    List<Application> findByJobSeeker(JobSeekerProfile jobSeeker);

    // 특정 구직자가 특정 게시물에 이미 신청했는지 확인
    boolean existsByJobSeekerAndJobPost(JobSeekerProfile jobSeeker, JobPost jobPost);

    // 특정 게시물의 신청자 수
    long countByJobPost(JobPost jobPost);

    // ACCEPTED 상태인 신청만, 기간 필터링 + 태그 join fetch
    @Query("""
    SELECT a FROM Application a
    JOIN FETCH a.jobPost jp
    LEFT JOIN FETCH jp.jobPostTags jpt
    LEFT JOIN FETCH jpt.interest i
    WHERE a.jobSeeker = :jobSeeker
      AND a.status = 'ACCEPTED'
      AND jp.status = 'CLOSED'
      AND jp.startDate >= :start
      AND jp.endDate <= :end
""")
    List<Application> findAcceptedClosedApplicationsWithTags(
            @Param("jobSeeker") JobSeekerProfile jobSeeker,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // 특정 구직자의 특정 게시물 신청 조회
    Optional<Application> findByJobSeekerAndJobPost(JobSeekerProfile jobSeeker, JobPost jobPost);

    // 특정 공고에 신청한 모든 신청 조회
    List<Application> findByJobPost(JobPost jobPost);

    // 특정 구직자와 특정 공고에 대한 신청 조회
    Optional<Application> findByJobSeeker_User_IdAndJobPost_Id(Long jobSeekerUserId, Long jobPostId);

    // 필요 시 특정 구직자의 특정 상태 신청 리스트 조회
    List<Application> findByJobSeekerAndStatus(JobSeekerProfile jobSeeker, String status);

    // 필요 시 특정 게시물의 특정 상태 신청자 리스트 조회
    List<Application> findByJobPostAndStatus(JobPost jobPost, String status);

}