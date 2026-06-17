package com.growvy.repository;

import com.growvy.entity.JobPostSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostScheduleRepository extends JpaRepository<JobPostSchedule, Long> {

    List<JobPostSchedule> findByJobPostId(Long jobPostId);

}