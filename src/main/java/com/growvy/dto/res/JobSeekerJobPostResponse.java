package com.growvy.dto.res;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class JobSeekerJobPostResponse {

    private Long id;
    private String title;
    private String companyName;

    // 프론트엔드의 _calculateDDay(item['endDate']) 함수가 D-Day를 계산할 수 있도록 마감일 전달
    private LocalDateTime endDate;
    private String dDay;

    // 프론트엔드가 item['tags'][0] 구조를 사용하고 있으므로 List 형태로 태그 전달
    // (예: ["Part-time"] 또는 ["Casual"])
    private List<String> tags;

    // 아래 필드들은 프론트의 자원봉사 판별 및 상세 텍스트(body/hasContent) 로직용 최소 필드입니다.
    private double hourlyWage;     // wage == 0 이면 "Volunteer"로 프론트가 자동 분류함
    private String description;    // 프론트의 'body' 및 'hasContent'로 매핑됨
    private List<String> imageUrls; // 공고 이미지 리스트 (프론트의 'photos')
}