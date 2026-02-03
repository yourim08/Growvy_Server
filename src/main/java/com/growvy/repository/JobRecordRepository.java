package com.growvy.repository;

import com.growvy.entity.JobRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JobRecordRepository extends JpaRepository<JobRecord, Long> {
    Optional<JobRecord> findByApplicationId(Long applicationId);

    @Query("""
    SELECT r FROM JobRecord r
    JOIN Application a ON a.id = r.applicationId
    WHERE a.jobSeeker.user.id = :jobSeekerId
      AND a.jobPost.id = :jobPostId
""")
    Optional<JobRecord> findByJobSeekerIdAndJobPostId(
            @Param("jobSeekerId") Long jobSeekerId,
            @Param("jobPostId") Long jobPostId
    );

    @Query("""
    SELECT r FROM JobRecord r
    JOIN Application a ON a.id = r.applicationId
    WHERE a.jobPost.id = :jobPostId
""")
    Optional<JobRecord> findByJobPostId(@Param("jobPostId") Long jobPostId);

}
