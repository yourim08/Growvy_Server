package com.growvy.repository;

import com.growvy.entity.JobRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRecordRepository extends JpaRepository<JobRecord, Long> {
    // 필요시 findByApplicationId 등 추가 가능
    Optional<JobRecord> findByApplicationId(Long applicationId);

    Optional<JobRecord> findByJobSeekerIdAndJobPostId(Long jobSeekerId, Long jobPostId);
}
