package com.growvy.repository;

import com.growvy.entity.EmployerProfile;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    // 모든 공고 가져오기
    List<JobPost> findAllByIdNotInOrderByCreatedAtDesc(List<Long> excludedIds);

    // 최신 순 조회
    List<JobPost> findAllByOrderByCreatedAtDesc();

    // 인기 순 조회 (신청한 것 제외)
    List<JobPost> findAllByIdNotInOrderByViewDesc(List<Long> excludedIds);

    // 전체 인기순
    List<JobPost> findAllByOrderByViewDesc();

    // 최신순, OPEN인 것만
    List<JobPost> findAllByStatusOrderByCreatedAtDesc(JobPost.Status status);

    // 인기순, OPEN인 것만
    List<JobPost> findAllByStatusOrderByViewDesc(JobPost.Status status);

    // 내가 신청한 게시물 제외, 최신순, OPEN인 것만
    List<JobPost> findAllByIdNotInAndStatusOrderByCreatedAtDesc(List<Long> ids, JobPost.Status status);

    // 내가 신청한 게시물 제외, 인기순, OPEN인 것만
    List<JobPost> findAllByIdNotInAndStatusOrderByViewDesc(List<Long> ids, JobPost.Status status);

    // DONE 제외 공고 조회
    List<JobPost> findByUserAndStatusNot(User user, JobPost.Status status);

    // DONE 공고만 조회 (Employer)
    List<JobPost> findByUserAndStatus(User user, JobPost.Status status);
}
