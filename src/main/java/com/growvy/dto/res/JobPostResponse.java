package com.growvy.dto.res;

import com.growvy.entity.JobPost;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor // 역직렬화(Jackson 등)를 위한 기본 생성자
@AllArgsConstructor // @Builder와 @NoArgsConstructor 혼용 시 발생하는 에러 방지용 전체 생성자
public class JobPostResponse {

    private Long id;

    // 구인자
    private EmployerInfo employer;

    // 공고 기본 정보
    private String title;
    private String companyName;
    private String jobAddress;
    private String responsibility;
    private String description;

    // 기간
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitmentDeadline;

    // 모집 정보
    private int count;
    private double hourlyRates;
    private double penaltyRates;
    private JobPost.Superannuation superannuation;

    // 위치
    private Double lat;
    private Double lng;
    private String state;
    private String city;

    // 기타
    private Long view;
    private LocalDateTime createdAt;

    // 스케줄 (요일별 근무시간)
    private List<Schedule> schedules;

    // 태그
    private List<Tag> tags;

    // 이미지
    private List<String> imageUrls;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schedule {
        private String dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tag {
        private Long id;
        private String name;
    }
}