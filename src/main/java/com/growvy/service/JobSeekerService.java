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

    // 일 신청 API
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

    // 신청한 일 조회 - APPLIED 상태만
    public List<JobPostResponse> getMyAppliedJobs(JobSeekerProfile jobSeeker) {
        List<Application> applications = applicationRepository.findByJobSeeker(jobSeeker);

        return applications.stream()
                .filter(app -> app.getStatus() == Application.Status.APPLIED) // APPLIED만 조회
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt())) // 최근 신청이 위
                .map(app -> {
                    JobPost post = app.getJobPost();

                    JobPostResponse res = new JobPostResponse();
                    res.setId(post.getId());
                    res.setTitle(post.getTitle());
                    res.setCompanyName(post.getCompanyName());
                    res.setDescription(post.getDescription());
                    res.setCount(post.getCount());
                    res.setStartDate(post.getStartDate());
                    res.setEndDate(post.getEndDate());
                    res.setStartTime(post.getStartTime());
                    res.setEndTime(post.getEndTime());
                    res.setHourlyWage(post.getHourlyWage());
                    res.setJobAddress(post.getJobAddress());
                    res.setLat(post.getLat());
                    res.setLng(post.getLng());
                    res.setState(post.getState());
                    res.setCity(post.getCity());
                    res.setCreatedAt(post.getCreatedAt());
                    res.setStatus(post.getStatus().name());

                    // 이미지 URL
                    res.setImageUrls(post.getJobPostImages() != null
                            ? post.getJobPostImages().stream().map(JobPostImage::getImageUrl).toList()
                            : new ArrayList<>());

                    // 태그
                    res.setTags(post.getJobPostTags() != null
                            ? post.getJobPostTags().stream()
                            .map(tag -> tag.getInterest().getName())
                            .toList()
                            : new ArrayList<>());

                    return res;
                })
                .toList();
    }

    // 완료 일 조회 - DONE 상태만 + works/volunteer 분기
    @Transactional(readOnly = true)
    public List<JobPostResponse> getMyDoneJobs(JobSeekerProfile jobSeeker, String type) {
        List<Application> applications = applicationRepository.findByJobSeeker(jobSeeker).stream()
                .filter(app -> app.getJobPost().getStatus() == JobPost.Status.DONE)
                .filter(app -> {
                    if ("works".equalsIgnoreCase(type)) {
                        return app.getJobPost().getHourlyWage() != 0;
                    } else if ("volunteer".equalsIgnoreCase(type)) {
                        return app.getJobPost().getHourlyWage() == 0;
                    } else {
                        return true; // type 없으면 모두 포함
                    }
                })
                .sorted((a, b) -> b.getJobPost().getEndDate().compareTo(a.getJobPost().getEndDate()))
                .toList();

        return applications.stream()
                .map(app -> {
                    JobPost post = app.getJobPost();

                    JobPostResponse res = new JobPostResponse();
                    res.setId(post.getId());
                    res.setTitle(post.getTitle());
                    res.setCompanyName(post.getCompanyName());
                    res.setDescription(post.getDescription());
                    res.setCount(post.getCount());
                    res.setStartDate(post.getStartDate());
                    res.setEndDate(post.getEndDate());
                    res.setStartTime(post.getStartTime());
                    res.setEndTime(post.getEndTime());
                    res.setHourlyWage(post.getHourlyWage());
                    res.setJobAddress(post.getJobAddress());
                    res.setLat(post.getLat());
                    res.setLng(post.getLng());
                    res.setState(post.getState());
                    res.setCity(post.getCity());
                    res.setCreatedAt(post.getCreatedAt());
                    res.setStatus(post.getStatus().name());

                    // 이미지 URL
                    res.setImageUrls(post.getJobPostImages() != null
                            ? post.getJobPostImages().stream().map(JobPostImage::getImageUrl).toList()
                            : new ArrayList<>());

                    // 태그
                    res.setTags(post.getJobPostTags() != null
                            ? post.getJobPostTags().stream()
                            .map(tag -> tag.getInterest().getName())
                            .toList()
                            : new ArrayList<>());

                    return res;
                })
                .toList();
    }


    // 신청한 일 취소 API
    @Transactional
    public void cancelApplication(JobSeekerProfile seeker, Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        Application app = applicationRepository.findByJobSeekerAndJobPost(seeker, jobPost)
                .orElseThrow(() -> new IllegalStateException("신청 내역이 없습니다."));

        applicationRepository.delete(app);

        // 신청자 수 체크 후, CLOSED였다면 빈 자리 생기면 OPEN으로 변경
        long appliedCount = applicationRepository.countByJobPost(jobPost);
        if (jobPost.getStatus() == JobPost.Status.CLOSED && appliedCount < jobPost.getCount()) {
            jobPost.setStatus(JobPost.Status.OPEN);
            jobPostRepository.save(jobPost);
        }
    }
}

