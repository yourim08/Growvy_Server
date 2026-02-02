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

    // Employer가 올린 공고 조회 (DONE 제외)
    public List<JobPostResponse> getMyPosts(User employerUser) {
        List<JobPost> posts = jobPostRepository.findByUserAndStatusNot(
                employerUser.getEmployerProfile().getUser(), JobPost.Status.DONE
        );

        // DTO 변환 + 최근 생성 순 정렬
        return posts.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // 최근 생성이 위
                .map(post -> {
                    JobPostResponse res = new JobPostResponse();
                    res.setId(post.getId());
                    res.setTitle(post.getTitle());
                    res.setStartDate(post.getStartDate());
                    res.setEndDate(post.getEndDate());
                    res.setStartTime(post.getStartTime());
                    res.setEndTime(post.getEndTime());
                    res.setStatus(post.getStatus().name());
                    return res;
                })
                .toList();
    }

    // Employer가 올린 DONE 공고 조회 (끝난 일 기준)
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

        // DTO 변환 + endDate 기준 내림차순 정렬
        return posts.stream()
                .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                .map(post -> {
                    JobPostResponse res = new JobPostResponse();
                    res.setId(post.getId());
                    res.setTitle(post.getTitle());
                    res.setStartDate(post.getStartDate());
                    res.setEndDate(post.getEndDate());
                    res.setStartTime(post.getStartTime());
                    res.setEndTime(post.getEndTime());
                    res.setStatus(post.getStatus().name());
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

        // 5. 공고 상태 CLOSED로
        jobPost.setStatus(JobPost.Status.CLOSED);
        jobPostRepository.save(jobPost);
    }
}
