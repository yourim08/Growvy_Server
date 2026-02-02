package com.growvy.dto.req;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class JobPostRequest {
    private String title;
    private String companyName;
    private String description;
    private int count;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTime;
    private String endTime;
    private int hourlyWage;
    private String jobAddress;
    private List<Long> interestIds; // JobPostTag와 연결할 Interest ID 리스트
    private List<String> imageUrls; // img url 리스트 (최대 4개)
}
