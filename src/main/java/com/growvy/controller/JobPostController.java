package com.growvy.controller;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.req.JobSeekerSignUpRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService jobPostService;
    private final JwtUtil jwtProvider;
    private final UserRepository userRepository;

    // 모든 일 최신순 조회 API (신청한 것 제외)
    @Operation(summary = "최신순 조회 API", description = "모든 post를 최신순으로 조회")
    @GetMapping("/all")
    public List<JobPostResponse> getAllPostsExcludingMyApplications(
            @RequestHeader("Authorization") String header
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        JobSeekerProfile jobSeeker = user.getJobSeekerProfile();

        return jobPostService.getAllPostsExcludingMyApplications(jobSeeker);
    }

    // 모든 일 인기순 조회 API (신청한 것 제외)
    @Operation(summary = "인기순 조회 API", description = "모든 post를 인기순으로 조회")
    @GetMapping("/all/popular")
    public List<JobPostResponse> getAllPostsByPopularity(
            @RequestHeader("Authorization") String header
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        JobSeekerProfile jobSeeker = user.getJobSeekerProfile();

        return jobPostService.getAllPostsByPopularity(jobSeeker);
    }

    // 상세 조회 API
    @Operation(summary = "상세 조회 API", description = "개별 post를 상세 조회")
    @GetMapping("/{postId}")
    public JobPostResponse getPostDetail(
            @RequestHeader("Authorization") String header,
            @PathVariable Long postId
    ) {
        // 토큰 검증만
        String jwt = header.replace("Bearer ", "").trim();
        jwtProvider.getFirebaseUid(jwt);

        return jobPostService.getPostDetail(postId);
    }


    // 구인자 공고 등록 API
    @Operation(summary = "Employer-공고 등록 API", description = "공고 등록")
    @PostMapping("/upload")
    public ResponseEntity<JobPostResponse> createPost(
            @RequestHeader("Authorization") String header,
            @RequestBody JobPostRequest request
    ) {
        // JWT 추출
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        // 사용자 조회
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 게시물 생성 및 DTO 반환
        JobPostResponse res = jobPostService.createJobPost(user, request);
        return ResponseEntity.ok(res);
    }
}
