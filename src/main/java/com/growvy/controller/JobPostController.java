package com.growvy.controller;

import org.springframework.data.domain.Page;
import com.growvy.annotation.CurrentUser;
import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.req.JobSeekerSignUpRequest;
import com.growvy.dto.res.HiringJobPostResponse;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.dto.res.SignUpResponse;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.User;
import com.growvy.repository.UserRepository;
import com.growvy.service.JobPostService;
import com.growvy.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService jobPostService;
    private final JwtUtil jwtProvider;
    private final UserRepository userRepository;

    // [공통] 사용자 맞춤형 공고 리스트 조회
    @GetMapping("/jobseeker/recommended")
    public ResponseEntity<Page<HiringJobPostResponse>> getRecommendedJobPosts(
            @CurrentUser User user,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(
                jobPostService.getRecommendedJobPosts(user, pageable)
        );
    }

    // [JobSeeker] 공고 인기순 조회
    @GetMapping("/jobseeker/popular")
    public ResponseEntity<Page<HiringJobPostResponse>> getPopularJobPosts(
            @CurrentUser User user,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(
                jobPostService.getPopularJobPosts(user, pageable)
        );
    }


    @Operation(summary = "[공통] 상세 조회 API", description = "개별 post를 상세 조회")
    @GetMapping("/{jobPostId}")
    public ResponseEntity<JobPostResponse> getJobPost(
            @CurrentUser User user,
            @PathVariable Long jobPostId
    ) {
        return ResponseEntity.ok(
                jobPostService.getJobPost(jobPostId)
        );
    }

    // [Employer] 공고 등록 API
    @Operation(summary = "[Employer] 공고 등록 API", description = "공고 등록")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobPostResponse> createPost(
            @CurrentUser User user,

            @RequestPart("request") JobPostRequest request,

            @RequestPart(value = "images", required = false)
            List<MultipartFile> images
    ) {
        JobPostResponse res =
                jobPostService.createJobPost(user, request, images);

        return ResponseEntity.ok(res);
    }


//    @Operation(summary = "[JobSeeker] 특정 기간 공고 조회 API", description = "today, week, calender 공고 조회")
//    // 내가 신청한 일 중에 특정기간 조회
//    @GetMapping("my/range")
//    public List<JobPostResponse> getMyAcceptedClosedJobs(
//            @CurrentUser User user,
//            @RequestParam String startDate,
//            @RequestParam String endDate
//    ) {
//        JobSeekerProfile jobSeeker = user.getJobSeekerProfile();
//
//        return jobPostService.getMyAcceptedClosedJobs(jobSeeker, startDate, endDate);
//    }
}