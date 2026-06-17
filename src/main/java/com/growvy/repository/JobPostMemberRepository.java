package com.growvy.repository;

import com.growvy.entity.JobPost;
import com.growvy.entity.JobPostMember;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobPostMemberRepository extends JpaRepository<JobPostMember, Long> {

    List<JobPostMember> findByJobSeekerUserIdAndStatus(
            Long jobSeekerUserId,
            JobPostMember.Status status
    );

    // 특정 공고에서 일한 멤버와 유저 정보를 한 번에 가져오기
    @Query("""
            SELECT m FROM JobPostMember m 
            JOIN FETCH m.jobSeeker js 
            JOIN FETCH js.user u 
            WHERE m.jobPost.id = :postId
            """)
    List<JobPostMember> findByJobPostId(@Param("postId") Long postId);


    Optional<JobPostMember> findByJobPostIdAndJobSeekerUserIdAndStatus(
            Long jobPostId,
            Long userId,
            JobPostMember.Status status
    );

    @Query("""
            SELECT m.jobPost.id, COUNT(m) FROM JobPostMember m 
            WHERE m.jobPost.id IN :postIds 
            AND m.status = 'WORKING' 
            GROUP BY m.jobPost.id
            """)
    List<Object[]> countWorkingMembersByPostIds(@Param("postIds") List<Long> postIds);

    // 각 공고별로 상태가 'DONE'인 총 워커 수 조회
    @Query("""
            SELECT m.jobPost.id, COUNT(m) 
            FROM JobPostMember m 
            WHERE m.jobPost.id IN :postIds 
            AND m.status = 'DONE' 
            GROUP BY m.jobPost.id
            """)
    List<Object[]> countDoneMembersByPostIds(@Param("postIds") List<Long> postIds);

    List<JobPostMember> findByJobSeeker_UserIdAndStatus(
            Long userId,
            JobPostMember.Status status
    );}