package com.growvy.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class DoneJobPostResponse {

    // [식별자] 프론트에서 데이터 다룰 때 필수
    private Long id;

    // [공고 정보]
    private String title;
    private String companyName;    // 프론트의 'employer'로 매핑

    // [상태 및 태그]
    private String dDay;
    private String employmentTag;  // 프론트의 'tag'로 매핑
    private String employerStatus; // 항상 "done" 고정

    // [비즈니스 로직 필드]
    private boolean reviewedAll;
}