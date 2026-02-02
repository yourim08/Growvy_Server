package com.growvy.service;
import com.growvy.dto.req.JobPostRequest;
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

    // Employer가 올린 공고 조회 (DONE 제외)
    public List<JobPostResponse> getMyPosts(User employerUser) {
        List<JobPost> posts = jobPostRepository.findByEmployerAndStatusNot(
                employerUser.getEmployerProfile(), JobPost.Status.DONE
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
    public List<JobPostResponse> getMyDonePosts(User employerUser) {
        List<JobPost> posts = jobPostRepository.findByEmployerAndStatus(
                employerUser.getEmployerProfile(), JobPost.Status.DONE
        );

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
}
