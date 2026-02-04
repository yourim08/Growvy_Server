package com.growvy.service;
import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.ApplicationResponse;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.*;
import com.growvy.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    // Employer가 올린 OPEN / CLOSED 공고 조회
    public List<JobPostResponse> getMyPosts(User employerUser) {
        List<JobPost.Status> statuses = List.of(JobPost.Status.OPEN, JobPost.Status.CLOSED);

        List<JobPost> posts = jobPostRepository.findByUserAndStatusIn(
                employerUser, statuses
        );

        return posts.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(post -> {
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
                    res.setCity(post.getCity());
                    res.setState(post.getState());
                    res.setCreatedAt(post.getCreatedAt());
                    res.setStatus(post.getStatus().name());
                    res.setTags(post.getJobPostTags().stream()
                            .map(tag -> tag.getInterest().getName())
                            .toList());
                    res.setImageUrls(post.getJobPostImages() != null
                            ? post.getJobPostImages().stream()
                            .map(JobPostImage::getImageUrl)
                            .toList()
                            : new ArrayList<>());
                    return res;
                })
                .toList();
    }


    // Employer가 올린 DONE 공고 조회
    public List<JobPostResponse> getMyDonePosts(User employerUser, String type) {
        List<JobPost> posts;

        if ("works".equalsIgnoreCase(type)) {
            posts = jobPostRepository.findByUserAndStatusAndHourlyWageNot(
                    employerUser.getEmployerProfile().getUser(), JobPost.Status.DONE, 0
            );
        } else if ("volunteer".equalsIgnoreCase(type)) {
            posts = jobPostRepository.findByUserAndStatusAndHourlyWage(
                    employerUser.getEmployerProfile().getUser(), JobPost.Status.DONE, 0
            );
        } else {
            posts = jobPostRepository.findByUserAndStatus(employerUser.getEmployerProfile().getUser(), JobPost.Status.DONE);
        }

        return posts.stream()
                .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                .map(post -> {
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
                    res.setCity(post.getCity());
                    res.setState(post.getState());
                    res.setCreatedAt(post.getCreatedAt());
                    res.setStatus(post.getStatus().name());
                    res.setTags(post.getJobPostTags().stream()
                            .map(tag -> tag.getInterest().getName())
                            .toList());
                    res.setImageUrls(post.getJobPostImages() != null
                            ? post.getJobPostImages().stream()
                            .map(JobPostImage::getImageUrl) // 여기서 직접 imageUrl 가져오기
                            .toList()
                            : new ArrayList<>());
                    return res;
                })
                .toList();
    }


    // 신청한 사람 조회
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicantsForJobPost(Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        List<Application> applications = applicationRepository.findByJobPost(jobPost);

        return applications.stream()
                .sorted((a, b) -> b.getAppliedAt().compareTo(a.getAppliedAt())) // 최근 신청 위
                .map(app -> {
                    ApplicationResponse res = new ApplicationResponse();
                    res.setApplicationId(app.getId());
                    res.setStatus(app.getStatus().name());

                    User user = app.getJobSeeker().getUser();
                    res.setName(user.getName());
                    res.setGender(user.getGender());
                    res.setAverageRating(user.getAverageRating());

                    return res;
                })
                .toList();
    }

    @Transactional
    public void acceptApplicants(Long jobPostId, List<Long> selectedApplicationIds, User employer) {
        // 1. 공고 조회 및 소유자 체크
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        if (!jobPost.getUser().getId().equals(employer.getId())) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        // 2. 신청자 전체 조회
        List<Application> allApplications = applicationRepository.findByJobPost(jobPost);

        // 3. 선택 인원 수 검증
        if (selectedApplicationIds.size() > jobPost.getCount()) {
            throw new IllegalArgumentException("선택 인원이 공고 모집 인원을 초과합니다.");
        }

        // 4. 상태 업데이트
        for (Application app : allApplications) {
            if (selectedApplicationIds.contains(app.getId())) {
                app.setStatus(Application.Status.ACCEPTED);
            } else {
                app.setStatus(Application.Status.CANCELED);
            }
        }
        applicationRepository.saveAll(allApplications);
    }
}
