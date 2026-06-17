package com.growvy.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class OngoingJobPostResponse {
    // [식별자] 프론트엔드에서 카드 삭제/수정 매칭할 때 꼭 필요함!
    private Long id;

    // [기본 정보]
    private String title;
    private String companyName; // 프론트의 'employer'로 매핑됨

    // [인원 정보]
    private int count;             // 프론트의 'applicantsTotal'
    private int applicantsCurrent; // 프론트의 'applicantsCurrent'

    // [상태 및 태그]
    private String dDay;
    private String employmentTag;  // 프론트의 'tag'
    private String employerStatus; // 프론트의 'employerStatus' ("ongoing" 고정)

}