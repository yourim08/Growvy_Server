package com.growvy.service;

import com.growvy.dto.req.RecordUploadRequest;
import com.growvy.entity.Application;
import com.growvy.entity.JobRecord;
import com.growvy.entity.JobRecordImage;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.JobRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobRecordService {

    private final JobRecordRepository jobRecordRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public void saveRecord(
            Long jobSeekerId,
            Long jobPostId,
            RecordUploadRequest req,
            boolean isCompleted
    ) {
        Application application = applicationRepository
                .findByJobSeeker_User_IdAndJobPost_Id(jobSeekerId, jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고에 신청 내역이 없습니다."));

        // 🔥 기존 record 조회 or 생성
        JobRecord record = jobRecordRepository.findByApplicationId(application.getId())
                .orElseGet(() -> {
                    JobRecord r = new JobRecord();
                    r.setApplicationId(application.getId());
                    return r;
                });

        // 내용 업데이트
        record.setTitle(req.getTitle());
        record.setContent(req.getContent());
        record.setCompanyName(req.getCompanyName());
        record.setPostTitle(req.getPostTitle());
        record.setIsCompleted(isCompleted);

        // 이미지 처리
        record.getRecordImages().clear();
        if (req.getImageUrls() != null) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                JobRecordImage image = new JobRecordImage();
                image.setJobRecord(record);
                image.setImageUrl(req.getImageUrls().get(i));
                image.setSortOrder(i);
                record.getRecordImages().add(image);
            }
        }

        jobRecordRepository.save(record);
        application.setStatus(Application.Status.DONE);
        // 영속 상태면 save 안 해도 되지만 명시적으로 해도 됨
        applicationRepository.save(application);
    }


    @Transactional(readOnly = true)
    public JobRecord getRecord(Long jobSeekerId, Long jobPostId) {
        return jobRecordRepository.findByJobSeekerIdAndJobPostId(jobSeekerId, jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고에 기록이 존재하지 않습니다."));
    }

    @Transactional
    public void deleteRecord(Long jobSeekerId, Long jobPostId) {
        JobRecord record = jobRecordRepository.findByJobSeekerIdAndJobPostId(jobSeekerId, jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고에 기록이 존재하지 않습니다."));

        // 이미지 제거 (연관 cascade 설정 없으면 필요)
        record.getRecordImages().clear();

        // 기록 삭제
        jobRecordRepository.delete(record);
    }

    @Transactional
    public void updateCompletedRecord(Long jobSeekerId, Long jobPostId, RecordUploadRequest req) {
        JobRecord record = jobRecordRepository.findByJobSeekerIdAndJobPostId(jobSeekerId, jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공고에 기록이 존재하지 않습니다."));

        if (!record.getIsCompleted()) {
            throw new IllegalStateException("임시 저장된 기록은 이 API로 수정할 수 없습니다.");
        }

        // 내용 업데이트
        record.setTitle(req.getTitle());
        record.setContent(req.getContent());
        record.setCompanyName(req.getCompanyName());
        record.setPostTitle(req.getPostTitle());

        // 이미지 처리
        record.getRecordImages().clear();
        if (req.getImageUrls() != null) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                JobRecordImage image = new JobRecordImage();
                image.setJobRecord(record);
                image.setImageUrl(req.getImageUrls().get(i));
                image.setSortOrder(i);
                record.getRecordImages().add(image);
            }
        }

        jobRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public JobRecord getPublicRecord(Long jobPostId) {
        JobRecord record = jobRecordRepository.findByJobPostId(jobPostId)
                .orElseThrow(() -> new IllegalArgumentException("공유 가능한 기록이 존재하지 않습니다."));

        // 공유 조건 제한
        if (!record.getIsCompleted()) {
            throw new IllegalStateException("완료된 기록만 공유할 수 있습니다.");
        }
        return record;
    }
}