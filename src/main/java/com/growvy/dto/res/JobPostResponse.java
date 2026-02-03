package com.growvy.dto.res;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class JobPostResponse {
    private Long id;
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
    private Double lat;
    private Double lng;
    private String state;
    private String city;
    private LocalDateTime createdAt;
    private String status;
    private List<String> tags;
    private List<String> imageUrls;
}
