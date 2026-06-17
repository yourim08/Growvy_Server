package com.growvy.repository;

import com.growvy.entity.JobPostTag;
import com.growvy.entity.JobPostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostTagRepository extends JpaRepository<JobPostTag, JobPostTagId> {
    List<JobPostTag> findByJobPostId(Long jobPostId);
}