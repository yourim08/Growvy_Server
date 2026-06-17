package com.growvy.service;

import org.springframework.data.domain.Page;
import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.HiringJobPostResponse;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.*;
import com.growvy.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostService {

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final JobPostTagRepository jobPostTagRepository;
    private final InterestRepository interestRepository;
    private final GeoService geoService;
    private final JobPostScheduleRepository jobPostScheduleRepository;
    private final JobPostImageRepository jobPostImageRepository;
    private final ImageService imageService;
    private final EmployerProfileRepository employerProfileRepository;

    // [Employer] 공고 생성
    @Transactional
    public JobPostResponse createJobPost(
            User user,
            JobPostRequest req,
            List<MultipartFile> images
    ) {
        // 1. 유저 ID로 고용주 프로필 조회
        EmployerProfile profile = employerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 고용주의 프로필을 찾을 수 없습니다."));

        JobPost jobPost = new JobPost();
        jobPost.setUser(user);

        // 2. Request 대신 Profile(DB)에서 회사명과 주소 꺼내오기
        jobPost.setCompanyName(profile.getCompanyName());
        jobPost.setJobAddress(profile.getBusinessAddress());

        jobPost.setTitle(req.getTitle());
        jobPost.setResponsibility(req.getResponsibility());
        jobPost.setDescription(req.getDescription());

        jobPost.setCount(req.getCount());

        jobPost.setStartDate(req.getStartDate());
        jobPost.setEndDate(req.getEndDate());
        jobPost.setRecruitmentDeadline(req.getRecruitmentDeadline());

        jobPost.setHourlyRates(req.getHourlyRates());
        jobPost.setPenaltyRates(req.getPenaltyRates());
        jobPost.setSuperannuation(req.getSuperannuation());

        // 위도 / 경도 (DB에서 가져온 주소 사용)
        Map<String, Double> coords = geoService.getCoordinates(profile.getBusinessAddress());

        if (coords == null
                || coords.get("lat") == null
                || coords.get("lng") == null) {
            throw new IllegalStateException("사업장 주소 좌표 변환 실패");
        }

        jobPost.setLat(coords.get("lat"));
        jobPost.setLng(coords.get("lng"));

        // city / state (DB에서 가져온 주소 파싱)
        String[] parsed = parseCityAndState(profile.getBusinessAddress());
        jobPost.setCity(parsed[0]);
        jobPost.setState(parsed[1]);

        JobPost savedJobPost = jobPostRepository.save(jobPost);


        List<JobPostResponse.Tag> tagResponses = new ArrayList<>();

        if (req.getInterestIds() != null) {

            for (Long interestId : req.getInterestIds()) {

                Interest interest = interestRepository.findById(interestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interest 없음 : " + interestId));

                JobPostTag tag = new JobPostTag();
                tag.setJobPost(savedJobPost);
                tag.setInterest(interest);
                tag.setId(
                        new JobPostTagId(
                                savedJobPost.getId(),
                                interestId
                        )
                );

                jobPostTagRepository.save(tag);

                tagResponses.add(
                        JobPostResponse.Tag.builder()
                                .id(interest.getId())
                                .name(interest.getName())
                                .build()
                );
            }
        }

        List<JobPostResponse.Schedule> scheduleResponses = new ArrayList<>();

        if (req.getSchedules() != null) {

            for (JobPostRequest.ScheduleRequest s : req.getSchedules()) {

                JobPostSchedule schedule = new JobPostSchedule();

                schedule.setJobPost(savedJobPost);
                schedule.setDayOfWeek(s.getDayOfWeek());
                schedule.setStartTime(s.getStartTime());
                schedule.setEndTime(s.getEndTime());

                jobPostScheduleRepository.save(schedule);

                scheduleResponses.add(
                        JobPostResponse.Schedule.builder()
                                .dayOfWeek(s.getDayOfWeek().name())
                                .startTime(s.getStartTime())
                                .endTime(s.getEndTime())
                                .build()
                );
            }
        }


        List<String> imageUrls = new ArrayList<>();

        if (images != null && !images.isEmpty()) {

            if (images.size() > 4) {
                throw new IllegalArgumentException(
                        "이미지는 최대 4개까지 업로드 가능합니다."
                );
            }

            int order = 1;

            for (MultipartFile image : images) {
                String savedPath =
                        imageService.saveJobPostImage(
                                savedJobPost.getId(),
                                image,
                                order
                        );

                JobPostImage jobPostImage = new JobPostImage();

                jobPostImage.setJobPost(savedJobPost);
                jobPostImage.setImageUrl(savedPath);
                jobPostImage.setSortOrder(order);

                jobPostImageRepository.save(jobPostImage);

                imageUrls.add(savedPath);

                order++;
            }
        }

        return JobPostResponse.builder()
                .id(savedJobPost.getId())

                .employer(
                        JobPostResponse.EmployerInfo.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .build()
                )

                .title(savedJobPost.getTitle())
                .companyName(savedJobPost.getCompanyName())
                .jobAddress(savedJobPost.getJobAddress())
                .responsibility(savedJobPost.getResponsibility())
                .description(savedJobPost.getDescription())

                .startDate(savedJobPost.getStartDate())
                .endDate(savedJobPost.getEndDate())
                .recruitmentDeadline(savedJobPost.getRecruitmentDeadline())

                .count(savedJobPost.getCount())
                .hourlyRates(savedJobPost.getHourlyRates())
                .penaltyRates(savedJobPost.getPenaltyRates())

                .superannuation(savedJobPost.getSuperannuation())

                .lat(savedJobPost.getLat())
                .lng(savedJobPost.getLng())
                .state(savedJobPost.getState())
                .city(savedJobPost.getCity())

                .view(savedJobPost.getView())
                .createdAt(savedJobPost.getCreatedAt())

                .tags(tagResponses)
                .schedules(scheduleResponses)
                .imageUrls(imageUrls)

                .build();
    }

    // [공통] Job Post 상세 조회
    @Transactional
    public JobPostResponse getJobPost(Long jobPostId) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() ->
                        new IllegalArgumentException("공고를 찾을 수 없습니다."));

        // 조회수 증가를 할 거면 여기서 처리
        jobPost.setView(jobPost.getView() + 1);

        List<JobPostResponse.Tag> tags =
                jobPostTagRepository.findByJobPostId(jobPostId)
                        .stream()
                        .map(tag ->
                                JobPostResponse.Tag.builder()
                                        .id(tag.getInterest().getId())
                                        .name(tag.getInterest().getName())
                                        .build()
                        )
                        .toList();

        List<JobPostResponse.Schedule> schedules =
                jobPostScheduleRepository.findByJobPostId(jobPostId)
                        .stream()
                        .map(schedule ->
                                JobPostResponse.Schedule.builder()
                                        .dayOfWeek(schedule.getDayOfWeek().name())
                                        .startTime(schedule.getStartTime())
                                        .endTime(schedule.getEndTime())
                                        .build()
                        )
                        .toList();

        List<String> imageUrls =
                jobPostImageRepository.findByJobPostIdOrderBySortOrderAsc(jobPostId)
                        .stream()
                        .map(JobPostImage::getImageUrl)
                        .toList();

        return JobPostResponse.builder()
                .id(jobPost.getId())

                .employer(
                        JobPostResponse.EmployerInfo.builder()
                                .id(jobPost.getUser().getId())
                                .name(jobPost.getUser().getName())
                                .build()
                )

                .title(jobPost.getTitle())
                .companyName(jobPost.getCompanyName())
                .jobAddress(jobPost.getJobAddress())
                .responsibility(jobPost.getResponsibility())
                .description(jobPost.getDescription())

                .startDate(jobPost.getStartDate())
                .endDate(jobPost.getEndDate())
                .recruitmentDeadline(jobPost.getRecruitmentDeadline())

                .count(jobPost.getCount())
                .hourlyRates(jobPost.getHourlyRates())
                .penaltyRates(jobPost.getPenaltyRates())
                .superannuation(jobPost.getSuperannuation())

                .lat(jobPost.getLat())
                .lng(jobPost.getLng())
                .state(jobPost.getState())
                .city(jobPost.getCity())

                .view(jobPost.getView())
                .createdAt(jobPost.getCreatedAt())

                .tags(tags)
                .schedules(schedules)
                .imageUrls(imageUrls)

                .build();
    }

    // [JobSeeker] 사용자 맞춤형 조회
    @Transactional(readOnly = true)
    public Page<HiringJobPostResponse> getRecommendedJobPosts(
            User loginUser,
            Pageable pageable) {

        // 1. DB에서 가중치(match_score) 기반으로 계산 및 정렬된 추천 공고 조회
        Page<JobPost> jobPosts =
                jobPostRepository.findRecommendedPostsForJobSeeker(
                        loginUser.getId(),
                        pageable
                );

        LocalDate today = LocalDate.now();

        // 2. 프론트엔드 요구사항에 맞춘 DTO 매핑
        return jobPosts.map(post -> {

            List<HiringJobPostResponse.Schedule> schedules =
                    post.getSchedules().stream()
                            .map(s -> HiringJobPostResponse.Schedule.builder()
                                    .dayOfWeek(s.getDayOfWeek().name())
                                    .startTime(s.getStartTime())
                                    .endTime(s.getEndTime())
                                    .build())
                            .toList();

            List<String> imageUrls =
                    post.getImages().stream()
                            .map(JobPostImage::getImageUrl)
                            .toList();

            String employmentTag =
                    post.getJobPostTags().stream()
                            .filter(tag ->
                                    "EMPLOYMENT".equals(tag.getInterest().getType().name()))
                            .map(tag -> tag.getInterest().getName())
                            .findFirst()
                            .orElse(null);

            LocalDate deadlineDate = post.getRecruitmentDeadline().toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);
            String dDay = daysBetween == 0 ? "D-Day" :
                    daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

            return HiringJobPostResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .companyName(post.getCompanyName())
                    .jobAddress(post.getJobAddress())
                    .description(post.getDescription())
                    .count(post.getCount())
                    .hourlyRates(post.getHourlyRates())
                    .startDate(post.getStartDate())
                    .endDate(post.getEndDate())
                    .dDay(dDay) // 안전하게 처리된 dDay 할당
                    .employmentTag(employmentTag)
                    .applicantsCurrent(0) // 추천 피드이므로 불필요한 카운트 쿼리 절약
                    .employerStatus("active")
                    .responsibility(post.getResponsibility())
                    .recruitmentDeadline(post.getRecruitmentDeadline())
                    .penaltyRates(post.getPenaltyRates())
                    .superannuation(
                            post.getSuperannuation() != null
                                    ? post.getSuperannuation().name()
                                    : null)
                    .lat(post.getLat())
                    .lng(post.getLng())
                    .city(post.getCity())
                    .state(post.getState())
                    .view(post.getView())
                    .createdAt(post.getCreatedAt())
                    .schedules(schedules)
                    .imageUrls(imageUrls)
                    .employer(
                            HiringJobPostResponse.EmployerInfo.builder()
                                    .id(post.getUser().getId())
                                    .name(post.getUser().getName())
                                    .build()
                    )
                    .build();
        });
    }

    // [JobSeeker] 공고 인기순 조회
    @Transactional(readOnly = true)
    public Page<HiringJobPostResponse> getPopularJobPosts(User loginUser, Pageable pageable) {

        // 1. 인기순 쿼리 호출
        Page<JobPost> jobPosts = jobPostRepository.findPopularPostsForJobSeeker(loginUser.getId(), pageable);

        LocalDate today = LocalDate.now();

        return jobPosts.map(post -> {
            // [중복 로직] 별도의 private 메서드로 빼면 더 깔끔합니다.
            // 아래는 매핑 로직의 핵심입니다.

            List<HiringJobPostResponse.Schedule> schedules = post.getSchedules().stream()
                    .map(s -> HiringJobPostResponse.Schedule.builder()
                            .dayOfWeek(s.getDayOfWeek().name())
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime())
                            .build())
                    .toList();

            List<String> imageUrls =
                    post.getImages().stream()
                            .map(JobPostImage::getImageUrl)
                            .toList();

            String employmentTag = post.getJobPostTags().stream()
                    .filter(tag -> "EMPLOYMENT".equals(tag.getInterest().getType().name()))
                    .map(tag -> tag.getInterest().getName())
                    .findFirst()
                    .orElse(null);

            LocalDate deadlineDate = post.getRecruitmentDeadline().toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);
            String dDay = daysBetween == 0 ? "D-Day" :
                    daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

            return HiringJobPostResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .companyName(post.getCompanyName())
                    .jobAddress(post.getJobAddress())
                    .description(post.getDescription())
                    .count(post.getCount())
                    .hourlyRates(post.getHourlyRates())
                    .startDate(post.getStartDate())
                    .endDate(post.getEndDate())
                    .dDay(dDay)
                    .employmentTag(employmentTag)
                    .applicantsCurrent(0)
                    .employerStatus("active")
                    .responsibility(post.getResponsibility())
                    .recruitmentDeadline(post.getRecruitmentDeadline())
                    .penaltyRates(post.getPenaltyRates())
                    .superannuation(
                            post.getSuperannuation() != null
                                    ? post.getSuperannuation().name()
                                    : null)
                    .lat(post.getLat())
                    .lng(post.getLng())
                    .city(post.getCity())
                    .state(post.getState())
                    .view(post.getView())
                    .createdAt(post.getCreatedAt())
                    .schedules(schedules)
                    .imageUrls(imageUrls)
                    .employer(
                            HiringJobPostResponse.EmployerInfo.builder()
                                    .id(post.getUser().getId())
                                    .name(post.getUser().getName())
                                    .build()
                    )
                    .build();
        });
    }



    // 주소를 쉼표 기준으로 파싱하여 city와 state 반환 (형식 고정 가정)
    private String[] parseCityAndState(String address) {
        if (address == null || address.isBlank()) {
            return new String[]{null, null};
        }
        String[] parts = address.split(",");
        String city = parts[1].trim();
        String state = parts[2].trim();
        return new String[]{city, state};
    }
}