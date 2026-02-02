package com.growvy.controller;

import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.User;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.JobSeekerProfileRepository;
import com.growvy.repository.UserRepository;
import com.growvy.service.JobSeekerService;
import com.growvy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jobseeker")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JwtUtil jwtProvider;
    private final UserRepository userRepository;
    private final JobSeekerService jobSeekerService;

    // 일 신청 API
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
}
