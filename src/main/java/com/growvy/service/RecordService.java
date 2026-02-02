package com.growvy.service;

import com.growvy.dto.req.RecordUploadRequest;
import com.growvy.entity.Record;
import com.growvy.entity.RecordImage;
import com.growvy.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;

    @Transactional
    public void uploadRecord(RecordUploadRequest req) {
        Record record = new Record();
        record.setApplicationId(req.getApplicationId());
        record.setTitle(req.getTitle());
        record.setContent(req.getContent());
        record.setCompanyName(req.getCompanyName());
        record.setPostTitle(req.getPostTitle());
        record.setIsCompleted(true); // 실제 저장

        // 이미지 추가
        if (req.getImageUrls() != null) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                RecordImage image = new RecordImage();
                image.setRecord(record);
                image.setImageUrl(req.getImageUrls().get(i));
                image.setSortOrder(i);
                record.getRecordImages().add(image);
            }
        }

        recordRepository.save(record);
    }
}
