package com.growvy.dto.res;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class HiringJobPostResponse {

    private Long id;

    // [기본 정보]
    private String title;
    private String companyName;
    private String jobAddress;
    private String responsibility;
    private String description;

    // [일정/시간 정보]
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitmentDeadline;

    // [급여 및 조건]
    private int count;
    private double hourlyRates;
    private double penaltyRates;
    private String superannuation; // Enum을 String으로 내려주는 것이 프론트에 안전함

    // [위치 및 기타]
    private Double lat;
    private Double lng;
    private String city;
    private String state;
    private Long view;
    private LocalDateTime createdAt;

    // [새로 추가된 필드들 (프론트 매핑용)]
    private String dDay;
    private String employmentTag;
    private int applicantsCurrent;
    private String employerStatus;

    // [중첩 객체 리스트]
    private List<Schedule> schedules;
    private List<String> imageUrls;
    private EmployerInfo employer;

    // --- 내부 DTO 클래스 선언 ---
    @Getter
    @Builder
    public static class Schedule {
        private String dayOfWeek;
        private LocalTime startTime; // 또는 LocalTime (기존 타입에 맞게)
        private LocalTime endTime;   // 또는 LocalTime
    }

    @Getter
    @Builder
    public static class EmployerInfo {
        private Long id;
        private String name;
    }
}