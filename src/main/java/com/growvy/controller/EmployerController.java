package com.growvy.controller;


import com.growvy.dto.res.JobPostResponse;
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

    @Operation(summary = "Employer-올린 일 목록 조회", description = "내가 올린 모든 일 조회-DONE제외")
    @GetMapping("/posts")
    public List<JobPostResponse> getMyPosts(
            @RequestHeader("Authorization") String header
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return employerService.getMyPosts(user);
    }

    @Operation(summary = "Employer-DONE 공고 조회", description = "내가 올린 DONE 공고 조회, 끝난 일 기준 내림차순")
    @GetMapping("/posts/done")
    public ResponseEntity<List<JobPostResponse>> getMyDonePosts(
            @RequestHeader("Authorization") String header
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<JobPostResponse> res = employerService.getMyDonePosts(user);
        return ResponseEntity.ok(res);
    }
}
