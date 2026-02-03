package com.growvy.service;

import com.growvy.dto.req.RecordUploadRequest;
import com.growvy.entity.Application;
import com.growvy.entity.Record;
import com.growvy.entity.RecordImage;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public void uploadRecord(Long jobSeekerId, Long jobPostId, RecordUploadRequest req) {
        // 1. 신청(Application) 조회
        Application application = applicationRepository.findByJobSeekerIdAndJobPostId(jobSeekerId, jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고에 신청 내역이 없습니다."));

        // 2. Record 생성
        Record record = new Record();
        record.setApplicationId(application.getId());
        record.setTitle(req.getTitle());
        record.setContent(req.getContent());
        record.setCompanyName(req.getCompanyName());
        record.setPostTitle(req.getPostTitle());
        record.setIsCompleted(true); // 실제 저장

        // 3. 이미지 추가
        if (req.getImageUrls() != null) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                RecordImage image = new RecordImage();
                image.setRecord(record);
                image.setImageUrl(req.getImageUrls().get(i));
                image.setSortOrder(i);
                record.getRecordImages().add(image);
            }
        }

        // 4. 저장
        recordRepository.save(record);
    }
}