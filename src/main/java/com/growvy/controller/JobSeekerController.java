package com.growvy.controller;

import com.growvy.annotation.CurrentUser;
import com.growvy.dto.req.NoteCreateRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.dto.res.JobSeekerJobPostResponse;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.User;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.JobSeekerProfileRepository;
import com.growvy.repository.UserRepository;
import com.growvy.service.JobSeekerService;
import com.growvy.service.NoteService;
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
    private final NoteService noteService;


    // [JobSeeker] 신청한 일 목록 조회 API
    @Operation(summary = "[JobSeeker] 지원한 공고 조회", description = "APPLIED 상태 공고")
    @GetMapping("/posts/applied")
    public List<JobSeekerJobPostResponse> getAppliedPosts(
            @CurrentUser User user
    ) {
        return jobSeekerService.getAppliedJobPosts(user);
    }


    // [JobSeeker] 진행중인 일 목록 조회 API
    @Operation(summary = "[JobSeeker] 진행중인 공고 조회", description = "ONGOING 상태 공고")
    @GetMapping("/posts/ongoing")
    public List<JobSeekerJobPostResponse> getOngoingPosts(
            @CurrentUser User user
    ) {
        return jobSeekerService.getOngoingJobPosts(user);
    }

    // [JobSeeker] 완료한 일 목록 조회 API
    @Operation(summary = "[JobSeeker] 완료한 공고 조회", description = "DONE 상태 공고")
    @GetMapping("/posts/done")
    public List<JobSeekerJobPostResponse> getDonePosts(
            @CurrentUser User user
    ) {
        return jobSeekerService.getDoneJobPosts(user);
    }

    // [JobSeeker] 공고 지원 API
    @Operation(summary = "[JobSeeker] 공고 지원", description = "특정 공고에 지원")
    @PostMapping("/posts/{postId}/apply")
    public ResponseEntity<Void> applyJobPost(
            @CurrentUser User user,
            @PathVariable Long postId
    ) {
        jobSeekerService.applyJobPost(user, postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[JobSeeker] 노트 작성", description = "완료한 일에 대한 노트 작성")
    @PostMapping("/notes")
    public ResponseEntity<Void> createNote(
            @CurrentUser User user,
            @RequestBody NoteCreateRequest req
    ) {
        noteService.createNote(user, req);
        return ResponseEntity.ok().build();
    }

}