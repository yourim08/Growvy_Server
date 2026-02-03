package com.growvy.controller;

import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.User;
import com.growvy.repository.UserRepository;
import com.growvy.service.JobPostSearchService;
import com.growvy.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobPostSearchController {

    private final JobPostSearchService jobPostSearchService;
    private final JwtUtil jwtProvider;
    private final UserRepository userRepository;

    @Operation(summary = "검색 API", description = "keyword 포함 게시물 반환")
    @GetMapping("/search")
    public ResponseEntity<List<JobPostResponse>> searchJobPosts(
            @RequestHeader("Authorization") String header,
            @RequestParam String keyword,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city
    ) {
        // 1. JWT에서 User 조회
        String jwt = header.replace("Bearer ", "").trim();
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 검색 수행 + 기록 저장
        List<JobPostResponse> results = jobPostSearchService.searchJobPosts(user, keyword, state, city);

        return ResponseEntity.ok(results);
    }
}