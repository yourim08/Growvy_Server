package com.growvy.controller;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.Application;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.User;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.JobSeekerProfileRepository;
import com.growvy.repository.UserRepository;
import com.growvy.service.JobPostService;
import com.growvy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobseeker")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JobPostRepository jobPostRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final ApplicationRepository applicationRepository;
    private final JwtUtil jwtProvider;


    // 일 신청 API
    @PostMapping("/apply")
    public ResponseEntity<String> applyJob(
            @RequestHeader("Authorization") String header,
            @RequestBody Map<String, Long> body
    ) {
        // 1. JWT로 사용자 조회
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt); // Firebase UID 추출
        JobSeekerProfile seeker = jobSeekerProfileRepository.findByUserFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("구직자를 찾을 수 없습니다."));

        // 2. 신청할 게시물 조회
        Long jobPostId = body.get("jobPostId");
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        if (jobPost.getStatus() == JobPost.Status.CLOSED) {
            return ResponseEntity.badRequest().body("이미 마감된 일입니다.");
        }

        // 3. 이미 신청했는지 확인
        boolean alreadyApplied = applicationRepository.existsByJobSeekerAndJobPost(seeker, jobPost);
        if (alreadyApplied) {
            return ResponseEntity.badRequest().body("이미 신청한 일입니다.");
        }

        // 4. application 추가
        Application app = new Application();
        app.setJobPost(jobPost);
        app.setJobSeeker(seeker);
        app.setStatus(Application.Status.valueOf("APPLIED"));
        app.setAppliedAt(LocalDateTime.now());
        applicationRepository.save(app);

        // 5. 지원자 수 체크 후 최대인원 도달하면 CLOSED
        long appliedCount = applicationRepository.countByJobPost(jobPost);
        if (appliedCount >= jobPost.getCount()) {
            jobPost.setStatus(JobPost.Status.CLOSED);
            jobPostRepository.save(jobPost);
        }

        return ResponseEntity.ok("신청 완료");
    }
}
