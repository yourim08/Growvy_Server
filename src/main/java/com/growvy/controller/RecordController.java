package com.growvy.controller;

import com.growvy.dto.req.RecordUploadRequest;
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

    @Operation(summary = "jobseeker-기록 업로드", description = "실제 완료된 기록 저장 (is_completed=true)")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadRecord(@RequestBody RecordUploadRequest req) {
        recordService.uploadRecord(req);
        return ResponseEntity.ok("기록이 저장되었습니다.");
    }
}
