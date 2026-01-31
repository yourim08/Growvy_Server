package com.growvy.service;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.*;
import com.growvy.repository.ApplicationRepository;
import com.growvy.repository.InterestRepository;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.JobPostTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostService {

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final JobPostTagRepository jobPostTagRepository;
    private final InterestRepository interestRepository;
    private final GeoService geoService;

    // 내가 신청한 일 목록 (조회 API)
    public List<JobPostResponse> getAcceptedPosts(JobSeekerProfile jobSeeker, LocalDate start, LocalDate end) {
        // 기본값 처리
        if (start == null) start = LocalDate.now();
        if (end == null) end = LocalDate.now();

        List<Application> applications = applicationRepository.findAcceptedApplicationsWithTags(jobSeeker, start, end);

        return applications.stream().map(a -> {
            JobPost jp = a.getJobPost();

            List<String> tags = jp.getJobPostTags().stream()
                    .map(jpt -> jpt.getInterest().getName())
                    .collect(Collectors.toList());

            JobPostResponse dto = new JobPostResponse();
            dto.setTitle(jp.getTitle());
            dto.setCompanyName(jp.getCompanyName());
            dto.setStartTime(jp.getStartTime());
            dto.setEndTime(jp.getEndTime());
            dto.setTags(tags);

            return dto;
        }).collect(Collectors.toList());
    }

    // 글 등록 API
    @Transactional
    public JobPostResponse createJobPost(User user, JobPostRequest req) {

        // 1. 게시글 생성
        JobPost jobPost = new JobPost();
        jobPost.setUser(user);
        jobPost.setTitle(req.getTitle());
        jobPost.setCompanyName(req.getCompanyName());
        jobPost.setDescription(req.getDescription());
        jobPost.setCount(req.getCount());
        jobPost.setStartDate(req.getStartDate());
        jobPost.setEndDate(req.getEndDate());
        jobPost.setStartTime(req.getStartTime());
        jobPost.setEndTime(req.getEndTime());
        jobPost.setHourlyWage(req.getHourlyWage());
        jobPost.setJobAddress(req.getJobAddress());
        jobPost.setStatus(JobPost.Status.OPEN);

        // 위도, 경도 가져오기
        Map<String, Double> coords = geoService.getCoordinates(req.getJobAddress());
        if (coords == null || coords.get("lat") == null || coords.get("lng") == null) {
            throw new IllegalStateException("사업장 주소 좌표 변환 실패");
        }
        log.info("JobPost에 넣는 좌표: lat={}, lng={}", coords.get("lat"), coords.get("lng"));
        jobPost.setLat(coords.get("lat"));
        jobPost.setLng(coords.get("lng"));

        JobPost savedJobPost = jobPostRepository.save(jobPost);

// 2. 태그 연결
        List<JobPostTag> savedTags = new ArrayList<>();
        if (req.getInterestIds() != null && !req.getInterestIds().isEmpty()) {
            for (Long interestId : req.getInterestIds()) {
                Interest interest = interestRepository.findById(interestId)
                        .orElseThrow(() -> new IllegalArgumentException("Interest가 존재하지 않음: " + interestId));

                JobPostTag tag = new JobPostTag();
                tag.setJobPost(savedJobPost);
                tag.setInterest(interest);
                tag.setId(new JobPostTagId(savedJobPost.getId(), interestId));

                jobPostTagRepository.save(tag);
                savedTags.add(tag);  // 리스트에 저장
            }
        }

// 3. DTO 반환
        JobPostResponse res = new JobPostResponse();
        res.setId(savedJobPost.getId());
        res.setTitle(savedJobPost.getTitle());
        res.setCompanyName(savedJobPost.getCompanyName());
        res.setDescription(savedJobPost.getDescription());
        res.setCount(savedJobPost.getCount());
        res.setStartDate(savedJobPost.getStartDate());
        res.setEndDate(savedJobPost.getEndDate());
        res.setStartTime(savedJobPost.getStartTime());
        res.setEndTime(savedJobPost.getEndTime());
        res.setHourlyWage(savedJobPost.getHourlyWage());
        res.setJobAddress(savedJobPost.getJobAddress());
        res.setLat(savedJobPost.getLat());
        res.setLng(savedJobPost.getLng());
        res.setStatus(savedJobPost.getStatus().name());
        res.setCreatedAt(savedJobPost.getCreatedAt());
        res.setTags(savedTags.stream()
                .map(jpt -> jpt.getInterest().getName())
                .toList());
        res.setSuccess(true);

        return res;
    }
}
