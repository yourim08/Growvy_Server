package com.growvy.controller;

import com.growvy.dto.req.RecordUploadRequest;
import com.growvy.service.AuthService;
import com.growvy.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;
    private final AuthService authService;

    @Operation(summary = "jobseeker-기록 업로드", description = "실제 완료된 기록 저장 (is_completed=true)")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadRecord(
            @RequestHeader("Authorization") String header,
            @RequestParam Long jobPostId,           // 선택한 공고 ID
            @RequestBody RecordUploadRequest req
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        Long jobSeekerId = authService.getJobSeekerProfileByJwt(jwt).getUser().getId();

        recordService.uploadRecord(jobSeekerId, jobPostId, req);
        return ResponseEntity.ok("기록이 저장되었습니다.");
    }
}
