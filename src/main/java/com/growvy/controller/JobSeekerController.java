package com.growvy.controller;

import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.User;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.JobSeekerProfileRepository;
import com.growvy.repository.UserRepository;
import com.growvy.service.JobSeekerService;
import com.growvy.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobseeker")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JwtUtil jwtProvider;
    private final UserRepository userRepository;
    private final JobSeekerService jobSeekerService;

    // 일 신청 API
    @Operation(summary = "JobSeeker-공고 신청 API", description = "공고 신청 - 만약 count가 full이면 CLOSED로 변경")
    @PostMapping("/apply")
    public ResponseEntity<String> applyJob(
            @RequestHeader("Authorization") String header,
            @RequestBody Map<String, Long> body
    ) {
        // 1. JWT 파싱
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        // 2. User 조회
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. JobSeekerProfile 조회
        JobSeekerProfile seeker = user.getJobSeekerProfile();
        if (seeker == null) {
            throw new IllegalStateException("구직자 프로필이 없습니다.");
        }

        // 4. 요청값
        Long jobPostId = body.get("jobPostId");
        if (jobPostId == null) {
            throw new IllegalArgumentException("jobPostId가 필요합니다.");
        }

        // 5. 서비스 호출
        jobSeekerService.applyJob(seeker, jobPostId);

        return ResponseEntity.ok("신청 완료");
    }

    // 신청한 일 목록 조회 API
    @Operation(summary = "JobSeeker-신청한 일 목록 조회", description = "내가 신청한 모든 일 조회")
    @GetMapping("/posts")
    public ResponseEntity<List<JobPostResponse>> getMyAppliedJobs(
            @RequestHeader("Authorization") String header
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        JobSeekerProfile jobSeeker = user.getJobSeekerProfile();

        List<JobPostResponse> res = jobSeekerService.getMyAppliedJobs(jobSeeker);
        return ResponseEntity.ok(res);
    }

    // 신청한 일 취소 API
    @Operation(summary = "JobSeeker-신청한 일 취소", description = "내가 신청한 일 취소")
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelJob(
            @RequestHeader("Authorization") String header,
            @RequestBody Map<String, Long> body
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        JobSeekerProfile seeker = user.getJobSeekerProfile();

        Long jobPostId = body.get("jobPostId");

        jobSeekerService.cancelApplication(seeker, jobPostId);

        return ResponseEntity.ok("신청 취소 완료");
    }

}
