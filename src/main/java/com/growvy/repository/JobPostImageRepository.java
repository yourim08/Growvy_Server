package com.growvy.repository;

import com.growvy.entity.JobPost;
import com.growvy.entity.JobPostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostImageRepository extends JpaRepository<JobPostImage, Long> {
}

