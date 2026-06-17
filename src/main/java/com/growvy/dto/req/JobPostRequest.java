package com.growvy.dto.req;

import com.growvy.entity.JobPost;
import com.growvy.entity.JobPostSchedule;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class JobPostRequest {
    // 기본 정보
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

    // 시급 및 수당 (int -> double 수정)
    private double hourlyRates;
    private double penaltyRates;

    private JobPost.Superannuation superannuation;

    // 태그
    private List<Long> interestIds;

    // 요일별 스케줄
    private List<ScheduleRequest> schedules;

    @Getter
    @Setter
    public static class ScheduleRequest {
        private JobPostSchedule.DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}