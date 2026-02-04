package com.growvy.service;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.*;
import com.growvy.repository.*;
import org.springframework.transaction.annotation.Transactional;
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
    private final JobPostImageRepository jobPostImageRepository;

    // 모든 OPEN 공고 중 내가 신청하지 않은 것만 최신순 조회
    @Transactional(readOnly = true)
    public List<JobPostResponse> getAllPostsExcludingMyApplications(JobSeekerProfile jobSeeker) {

        // 1. 내가 신청한 공고 ID 목록
        List<Long> appliedJobIds = applicationRepository.findByJobSeeker(jobSeeker)
                .stream()
                .map(app -> app.getJobPost().getId())
                .toList();

        // 2. OPEN 상태 + 미신청 공고만 조회
        List<JobPost> posts = appliedJobIds.isEmpty()
                ? jobPostRepository.findAllByStatusOrderByCreatedAtDesc(JobPost.Status.OPEN)
                : jobPostRepository.findAllByIdNotInAndStatusOrderByCreatedAtDesc(
                appliedJobIds,
                JobPost.Status.OPEN
        );

        // 3. DTO 변환
        return posts.stream().map(jp -> {
            JobPostResponse res = new JobPostResponse();
            res.setId(jp.getId());
            res.setTitle(jp.getTitle());
            res.setCompanyName(jp.getCompanyName());
            res.setDescription(jp.getDescription());
            res.setCount(jp.getCount());
            res.setStartDate(jp.getStartDate());
            res.setEndDate(jp.getEndDate());
            res.setStartTime(jp.getStartTime());
            res.setEndTime(jp.getEndTime());
            res.setHourlyWage(jp.getHourlyWage());
            res.setJobAddress(jp.getJobAddress());
            res.setLat(jp.getLat());
            res.setLng(jp.getLng());
            res.setCreatedAt(jp.getCreatedAt());
            res.setStatus(jp.getStatus().name());
            res.setTags(
                    jp.getJobPostTags().stream()
                            .map(tag -> tag.getInterest().getName())
                            .toList()
            );
            return res;
        }).toList();
    }



    // 인기순 정렬 API (조회수 기반)
    public List<JobPostResponse> getAllPostsByPopularity(JobSeekerProfile jobSeeker) {

        // 1. 내가 신청한 게시물 ID
        List<Long> appliedJobIds = applicationRepository.findByJobSeeker(jobSeeker)
                .stream()
                .map(app -> app.getJobPost().getId())
                .toList();

        // 2. 인기순 조회
        List<JobPost> posts;
        if (appliedJobIds.isEmpty()) {
            posts = jobPostRepository.findAllByStatusOrderByViewDesc(JobPost.Status.OPEN);
        } else {
            posts = jobPostRepository.findAllByIdNotInAndStatusOrderByViewDesc(appliedJobIds, JobPost.Status.OPEN);
        }

        // 3. DTO 변환
        return posts.stream().map(jp -> {
            JobPostResponse res = new JobPostResponse();
            res.setId(jp.getId());
            res.setTitle(jp.getTitle());
            res.setCompanyName(jp.getCompanyName());
            res.setDescription(jp.getDescription());
            res.setCount(jp.getCount());
            res.setStartDate(jp.getStartDate());
            res.setEndDate(jp.getEndDate());
            res.setStartTime(jp.getStartTime());
            res.setEndTime(jp.getEndTime());
            res.setHourlyWage(jp.getHourlyWage());
            res.setJobAddress(jp.getJobAddress());
            res.setLat(jp.getLat());
            res.setLng(jp.getLng());
            res.setCreatedAt(jp.getCreatedAt());
            res.setStatus(jp.getStatus().name());
            res.setTags(
                    jp.getJobPostTags().stream()
                            .map(tag -> tag.getInterest().getName())
                            .toList()
            );
            return res;
        }).toList();
    }


    // 상세 조회 API
    @Transactional
    public JobPostResponse getPostDetail(Long postId) {

        JobPost jobPost = jobPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        // 조회수 증가
        Long view = jobPost.getView();
        jobPost.setView(view == null ? 1L : view + 1);

        JobPostResponse res = new JobPostResponse();
        res.setId(jobPost.getId());
        res.setTitle(jobPost.getTitle());
        res.setCompanyName(jobPost.getCompanyName());
        res.setDescription(jobPost.getDescription());
        res.setCount(jobPost.getCount());
        res.setStartDate(jobPost.getStartDate());
        res.setEndDate(jobPost.getEndDate());
        res.setStartTime(jobPost.getStartTime());
        res.setEndTime(jobPost.getEndTime());
        res.setHourlyWage(jobPost.getHourlyWage());
        res.setJobAddress(jobPost.getJobAddress());
        res.setLat(jobPost.getLat());
        res.setLng(jobPost.getLng());
        res.setCreatedAt(jobPost.getCreatedAt());
        res.setStatus(jobPost.getStatus().name());
        res.setTags(
                jobPost.getJobPostTags().stream()
                        .map(tag -> tag.getInterest().getName())
                        .toList()
        );
        res.setImageUrls(
                jobPost.getJobPostImages().stream()
                        .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                        .map(JobPostImage::getImageUrl)
                        .toList()
        );
        return res;
    }


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
        jobPost.setLat(coords.get("lat"));
        jobPost.setLng(coords.get("lng"));

        // city / state 파싱
        String[] parsed = parseCityAndState(req.getJobAddress());
        jobPost.setCity(parsed[0]);
        jobPost.setState(parsed[1]);

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
                savedTags.add(tag);
            }
        }

        // 3. 이미지 연결
        List<JobPostImage> savedImages = new ArrayList<>();
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                String url = req.getImageUrls().get(i);
                JobPostImage img = new JobPostImage();
                img.setJobPost(savedJobPost);
                img.setImageUrl(url);
                img.setSortOrder(i + 1);
                jobPostImageRepository.save(img);
                savedImages.add(img);
            }
        }

        // 4. DTO 반환
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
        res.setCity(savedJobPost.getCity());
        res.setState(savedJobPost.getState());
        res.setStatus(savedJobPost.getStatus().name());
        res.setCreatedAt(savedJobPost.getCreatedAt());
        res.setTags(savedTags.stream()
                .map(jpt -> jpt.getInterest().getName())
                .toList());
        res.setImageUrls(savedImages.stream()
                .map(JobPostImage::getImageUrl)
                .toList());
        return res;
    }

    // 주소를 쉼표 기준으로 파싱하여 city와 state 반환
    private String[] parseCityAndState(String address) {
        if (address == null || address.isBlank()) {
            return new String[]{null, null};
        }
        String[] parts = address.split(",");
        String city = parts[1].trim();
        String state = parts[2].trim();
        return new String[]{city, state};
    }


    // 신청한 일 중에 특정 기간 조회
    public List<JobPostResponse> getMyAcceptedClosedJobs(JobSeekerProfile jobSeeker, String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        List<Application> apps = applicationRepository.findAcceptedClosedApplicationsWithTags(
                jobSeeker, startDate, endDate
        );

        // DTO 변환: 필요한 정보만
        return apps.stream()
                .map(app -> {
                    JobPost jp = app.getJobPost();
                    JobPostResponse res = new JobPostResponse();
                    res.setId(jp.getId());
                    res.setTitle(jp.getTitle());
                    res.setStartDate(jp.getStartDate());
                    res.setEndDate(jp.getEndDate());
                    res.setStartTime(jp.getStartTime());
                    res.setEndTime(jp.getEndTime());
                    return res;
                })
                .toList();
    }
}