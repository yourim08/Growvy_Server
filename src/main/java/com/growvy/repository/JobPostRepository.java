package com.growvy.repository;

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
}
