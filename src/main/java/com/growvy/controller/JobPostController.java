package com.growvy.controller;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.User;
import com.growvy.repository.UserRepository;
import com.growvy.service.JobPostService;
import com.growvy.util.JwtUtil;
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

    @GetMapping("/task")
    public List<JobPostResponse> getPosts(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String jwt
    ) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        JobSeekerProfile jobSeeker = user.getJobSeekerProfile();

        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : null;

        return jobPostService.getAcceptedPosts(jobSeeker, start, end);
    }

    @PostMapping("/upload")
    public ResponseEntity<JobPostResponse> createPost(
            @RequestBody JobPostRequest request,
            @RequestHeader("Authorization") String jwt
    ) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        JobPostResponse response = jobPostService.createJobPost(user, request);
        return ResponseEntity.ok(response); // 그냥 200 OK + DTO 반환
    }

}
