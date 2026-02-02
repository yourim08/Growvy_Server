package com.growvy.dto.req;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecordUploadRequest {
    private Long applicationId;   // 어떤 신청자 기록인지
    private String title;
    private String content;
    private String companyName;
    private String postTitle;
    private List<String> imageUrls; // RecordImage URL 리스트
}
