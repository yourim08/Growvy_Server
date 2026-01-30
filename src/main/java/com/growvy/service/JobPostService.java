package com.growvy.service;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.*;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.InterestRepository;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.JobPostTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final JobPostTagRepository jobPostTagRepository;
    private final InterestRepository interestRepository;

    // 내가 신청한 일 목록 (조회 API)
    public List<JobPostResponse> getAcceptedPosts(JobSeekerProfile jobSeeker, LocalDate start, LocalDate end) {
        // 기본값 처리
        if (start == null) start = LocalDate.now();
        if (end == null) end = LocalDate.now();

        List<Application> applications = applicationRepository.findAcceptedApplicationsWithTags(jobSeeker, start, end);

        return applications.stream().map(a -> {
            JobPost jp = a.getJobPost();

            List<String> tags = jp.getJobPostTags().stream()
                    .map(jpt -> jpt.getInterest().getName())
                    .collect(Collectors.toList());

            JobPostResponse dto = new JobPostResponse();
            dto.setTitle(jp.getTitle());
            dto.setCompanyName(jp.getCompanyName());
            dto.setStartTime(jp.getStartTime());
            dto.setEndTime(jp.getEndTime());
            dto.setTags(tags);

            return dto;
        }).collect(Collectors.toList());
    }

    // 글 등록 API
    @Transactional
    public JobPostResponse createJobPost(User user, JobPostRequest request) {

        // 1. 게시글 생성
        JobPost jobPost = new JobPost();
        jobPost.setUser(user);
        jobPost.setTitle(request.getTitle());
        jobPost.setCompanyName(request.getCompanyName());
        jobPost.setDescription(request.getDescription());
        jobPost.setCount(request.getCount());
        jobPost.setStartDate(request.getStartDate());
        jobPost.setEndDate(request.getEndDate());
        jobPost.setStartTime(request.getStartTime());
        jobPost.setEndTime(request.getEndTime());
        jobPost.setHourlyWage(request.getHourlyWage());
        jobPost.setJobAddress(request.getJobAddress());
        jobPost.setLat(request.getLat());
        jobPost.setLng(request.getLng());
        jobPost.setStatus(JobPost.Status.OPEN);

        // save 한 번만, Lambda에서 safe
        JobPost savedJobPost = jobPostRepository.save(jobPost);

        // 2. 태그 연결
        List<JobPostTag> tags = request.getInterestIds().stream()
                .map(interestId -> {
                    Interest interest = interestRepository.findById(interestId)
                            .orElseThrow(() -> new IllegalArgumentException("Interest가 존재하지 않음: " + interestId));
                    JobPostTag tag = new JobPostTag();
                    tag.setId(new JobPostTagId(savedJobPost.getId(), interestId));
                    tag.setJobPost(savedJobPost);
                    tag.setInterest(interest);
                    return tag;
                })
                .collect(Collectors.toList());

        jobPostTagRepository.saveAll(tags);
        savedJobPost.setJobPostTags(tags);

        // 3. DTO 반환
        JobPostResponse response = new JobPostResponse();
        response.setId(savedJobPost.getId());
        response.setTitle(savedJobPost.getTitle());
        response.setCompanyName(savedJobPost.getCompanyName());
        response.setDescription(savedJobPost.getDescription());
        response.setCount(savedJobPost.getCount());
        response.setStartDate(savedJobPost.getStartDate());
        response.setEndDate(savedJobPost.getEndDate());
        response.setStartTime(savedJobPost.getStartTime());
        response.setEndTime(savedJobPost.getEndTime());
        response.setHourlyWage(savedJobPost.getHourlyWage());
        response.setJobAddress(savedJobPost.getJobAddress());
        response.setTags(tags.stream().map(tag -> tag.getInterest().getName()).toList());

        return response;
    }
}
