package com.growvy.controller;

import com.growvy.annotation.CurrentUser;
import com.growvy.dto.req.ReviewRequest;
import com.growvy.dto.req.SelectApplicantsRequest;
import com.growvy.dto.res.*;
import com.growvy.entity.JobPost;
import com.growvy.entity.User;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.UserRepository;
import com.growvy.service.EmployerService;
import com.growvy.service.JobPostService;
import com.growvy.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employer")
@RequiredArgsConstructor
public class EmployerController {

    private final UserRepository userRepository;
    private final EmployerService employerService;
    private final JwtUtil jwtProvider;
    private final JobPostRepository jobPostRepository;

    @Operation(summary = "[Employer] 모집중인 일 목록 조회", description = "Hiring 상태의 모든 일 조회")
    @GetMapping("/posts/hiring")
    public List<HiringJobPostResponse> getHiringPosts(
            @CurrentUser User user
    ) {
        return employerService.getHiringPosts(user);
    }

    @Operation(summary = "[Employer] 진행중인 공고 조회", description = "Ongoing 상태의 모든 일 조회")
    @GetMapping("/posts/ongoing")
    public List<OngoingJobPostResponse> getOngoingPosts(
            @CurrentUser User user
    ) {
        return employerService.getOngoingPosts(user);
    }

    @Operation(summary = "[Employer] 끝난 공고 조회", description = "Done 상태의 모든 일 조회")
    @GetMapping("/posts/done")
    public List<DoneJobPostResponse> getDonePosts(
            @CurrentUser User user
    ) {
        return employerService.getDonePosts(user);
    }

    @Operation(
            summary = "[Employer] 지원자 목록 조회",
            description = "특정 공고에 지원한 사람 조회"
    )
    @GetMapping("/posts/{postId}/applicants")
    public List<ApplicationResponse> getApplicants(
            @CurrentUser User employer,
            @PathVariable Long postId
    ) {
        return employerService.getApplicants(employer, postId);
    }

    @Operation(
            summary = "[Employer] Done공고 리뷰 남길 사람 목록 조회",
            description = "Done 공고에 함께 일한 사람 중 리뷰 안남긴 사람 조회"
    )
    @GetMapping("/posts/{postId}/review-targets")
    public List<ReviewTargetResponse> getReviewTargets(
            @CurrentUser User employer,
            @PathVariable Long postId
    ) {
        return employerService.getReviewTargets(employer, postId);
    }

    @Operation(
            summary = "[Employer] 지원자 선발",
            description = "모집 인원만큼 지원자를 선발"
    )
    @PostMapping("/posts/{postId}/select")
    public ResponseEntity<Void> selectApplicants(
            @CurrentUser User employer,
            @PathVariable Long postId,
            @RequestBody SelectApplicantsRequest request
    ) {

        employerService.selectApplicants(
                employer,
                postId,
                request.getApplicationIds()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{postId}/reviews")
    @Operation(summary = "[Employer] 리뷰 작성", description = "구직자에게 리뷰를 작성합니다.")
    public ResponseEntity<Void> createReview(
            @CurrentUser User employer,
            @PathVariable Long postId,
            @RequestBody ReviewRequest request // 여기서 Setter를 이용해 값이 채워짐
    ) {
        employerService.createReview(employer, postId, request);
        return ResponseEntity.ok().build();
    }
}