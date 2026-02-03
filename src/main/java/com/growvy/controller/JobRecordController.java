package com.growvy.controller;

import com.growvy.dto.req.RecordUploadRequest;
import com.growvy.entity.JobRecord;
import com.growvy.service.AuthService;
import com.growvy.service.JobRecordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class JobRecordController {

    private final JobRecordService jobRecordService;
    private final AuthService authService;

    @Operation(summary = "JobSeeker-기록 업로드", description = "기록 저장 (isCompleted=true/false 선택 가능)")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadRecord(
            @RequestHeader("Authorization") String header,
            @RequestParam Long jobPostId,                  // 선택한 공고 ID
            @RequestBody RecordUploadRequest req,
            @RequestParam(defaultValue = "true") boolean isCompleted // 기본값 true
    ) {
        // JWT에서 JobSeeker ID 바로 조회
        String jwt = header.replace("Bearer ", "").trim();
        Long jobSeekerId = authService.getJobSeekerProfileByJwt(jwt).getUser().getId();

        // 서비스 호출
        jobRecordService.saveRecord(jobSeekerId, jobPostId, req, isCompleted);

        return ResponseEntity.ok("기록이 저장되었습니다.");
    }

    @Operation(summary = "JobSeeker-기록 조회", description = "선택한 공고의 작성된 기록 정보 조회")
    @GetMapping("/get")
    public ResponseEntity<JobRecord> getRecord(
            @RequestHeader("Authorization") String header,
            @RequestParam Long jobPostId
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        Long jobSeekerId = authService.getJobSeekerProfileByJwt(jwt).getUser().getId();

        JobRecord record = jobRecordService.getRecord(jobSeekerId, jobPostId);
        return ResponseEntity.ok(record);
    }

    @Operation(summary = "JobSeeker-기록 삭제", description = "선택한 공고의 작성된 기록 삭제")
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRecord(
            @RequestHeader("Authorization") String header,
            @RequestParam Long jobPostId
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        Long jobSeekerId = authService.getJobSeekerProfileByJwt(jwt).getUser().getId();

        jobRecordService.deleteRecord(jobSeekerId, jobPostId);
        return ResponseEntity.ok("기록이 삭제되었습니다.");
    }

    @Operation(summary = "JobSeeker-완료 기록 수정", description = "isCompleted=true인 기록만 수정 가능")
    @PutMapping("/update")
    public ResponseEntity<String> updateCompletedRecord(
            @RequestHeader("Authorization") String header,
            @RequestParam Long jobPostId,
            @RequestBody RecordUploadRequest req
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        Long jobSeekerId = authService.getJobSeekerProfileByJwt(jwt).getUser().getId();

        jobRecordService.updateCompletedRecord(jobSeekerId, jobPostId, req);
        return ResponseEntity.ok("완료된 기록이 수정되었습니다.");
    }

    @RestController
    @RequestMapping("/api/public/records")
    @RequiredArgsConstructor
    public class PublicJobRecordController {

        private final JobRecordService jobRecordService;

        @Operation(summary = "공유용 기록 조회", description = "JWT 없이 접근 가능한 기록 조회 API")
        @GetMapping("/{jobPostId}")
        public ResponseEntity<JobRecord> getPublicRecord(
                @PathVariable Long jobPostId
        ) {
            JobRecord record = jobRecordService.getPublicRecord(jobPostId);
            return ResponseEntity.ok(record);
        }
    }

}

