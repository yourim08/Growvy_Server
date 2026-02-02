package com.growvy.service;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.*;
import com.growvy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerService {

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;

    @Transactional
    public void applyJob(JobSeekerProfile seeker, Long jobPostId) {
        // 1. 신청할 게시물 조회
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        if (jobPost.getStatus() == JobPost.Status.CLOSED) {
            throw new IllegalStateException("이미 마감된 일입니다.");
        }

        // 2. 이미 신청했는지 확인
        boolean alreadyApplied = applicationRepository.existsByJobSeekerAndJobPost(seeker, jobPost);
        if (alreadyApplied) {
            throw new IllegalStateException("이미 신청한 일입니다.");
        }

        // 3. Application 생성
        Application app = new Application();
        app.setJobPost(jobPost);
        app.setJobSeeker(seeker);
        app.setStatus(Application.Status.APPLIED); // enum으로 변경
        app.setAppliedAt(LocalDateTime.now());
        applicationRepository.save(app);

        // 4. 지원자 수 체크 후 최대인원 도달하면 CLOSED
        long appliedCount = applicationRepository.countByJobPost(jobPost);
        if (appliedCount >= jobPost.getCount()) {
            jobPost.setStatus(JobPost.Status.CLOSED);
            jobPostRepository.save(jobPost);
        }
    }
}
