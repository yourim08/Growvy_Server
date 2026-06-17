package com.growvy.service;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.req.ReviewRequest;
import com.growvy.dto.res.*;
import com.growvy.entity.*;
import com.growvy.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final JobPostMemberRepository jobPostMemberRepository;
    private final ReviewRepository reviewRepository;

    // [Employer] Hiring 공고 조회
    @Transactional(readOnly = true)
    public List<HiringJobPostResponse> getHiringPosts(User employerUser) {

        // 1. 내가 올린 공고 리스트 조회 (1번의 쿼리)
        List<JobPost> posts = jobPostRepository.findHiringPosts(employerUser.getId());

        if (posts.isEmpty()) {
            return List.of();
        }

        // 2. 조회된 모든 공고의 ID들만 쏙 뽑아내기
        List<Long> postIds = posts.stream().map(JobPost::getId).toList();

        // 3. [최적화 🌟] DB에서 공고 ID들에 해당하는 지원자 수를 한방에 조회 (1번의 쿼리)
        List<Object[]> applicantCounts = applicationRepository.countApplicantsByPostIds(postIds);

        // 조회의 편의를 위해 Map<JobPostId, Count> 형태로 변환
        Map<Long, Long> applicantCountMap = applicantCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],  // jobPost.id
                        row -> (Long) row[1]   // COUNT(a)
                ));

        LocalDate today = LocalDate.now();

        // 4. 메모리에 다 올라온 데이터를 DTO로 매핑 (추가 쿼리 폭탄 없음!)
        return posts.stream()
                .map(post -> {
                    // application.yml의 default_batch_fetch_size 덕분에
                    // 아래의 컬렉션 stream 들은 N번 호출되지 않고 각각 딱 1번씩만 대량 조회(IN절)됩니다.
                    List<HiringJobPostResponse.Schedule> schedules = post.getSchedules().stream()
                            .map(s -> HiringJobPostResponse.Schedule.builder()
                                    .dayOfWeek(s.getDayOfWeek().name())
                                    .startTime(s.getStartTime())
                                    .endTime(s.getEndTime())
                                    .build())
                            .toList();

                    List<String> imageUrls = post.getImages().stream()
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

                    // [최적화 🌟] DB 안 가고 미리 만들어둔 Map에서 값을 꺼내옵니다. (0ms)
                    int applicantsCurrent = applicantCountMap.getOrDefault(post.getId(), 0L).intValue();

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
                            .applicantsCurrent(applicantsCurrent) // 매핑 완료
                            .employerStatus("hiring")
                            .responsibility(post.getResponsibility())
                            .recruitmentDeadline(post.getRecruitmentDeadline())
                            .penaltyRates(post.getPenaltyRates())
                            .superannuation(post.getSuperannuation() != null ? post.getSuperannuation().name() : null)
                            .lat(post.getLat())
                            .lng(post.getLng())
                            .city(post.getCity())
                            .state(post.getState())
                            .view(post.getView())
                            .createdAt(post.getCreatedAt())
                            .schedules(schedules)
                            .imageUrls(imageUrls)
                            .employer(HiringJobPostResponse.EmployerInfo.builder()
                                    .id(employerUser.getId())
                                    .name(employerUser.getName())
                                    .build())
                            .build();
                })
                .toList();
    }


    // [Employer] 진행중인 공고 조회
    @Transactional(readOnly = true)
    public List<OngoingJobPostResponse> getOngoingPosts(User employerUser) {

        // 1. 제안해주신 쿼리로 Ongoing 공고 목록 조회
        List<JobPost> posts = jobPostRepository.findOngoingPosts(employerUser.getId());

        if (posts.isEmpty()) {
            return List.of();
        }

        // 2. 공고 ID 리스트 추출
        List<Long> postIds = posts.stream().map(JobPost::getId).toList();

        // 3. [최적화] 각 공고별 'WORKING' 상태인 인원 수(applicantsCurrent) 한 번에 조회
        List<Object[]> workingCounts = jobPostMemberRepository.countWorkingMembersByPostIds(postIds);
        Map<Long, Long> workingCountMap = workingCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        LocalDate today = LocalDate.now();

        // 4. DTO 매핑
        return posts.stream()
                .map(post -> {
                    // 태그 추출
                    String employmentTag = post.getJobPostTags().stream()
                            .filter(tag -> "EMPLOYMENT".equals(tag.getInterest().getType().name()))
                            .map(tag -> tag.getInterest().getName())
                            .findFirst()
                            .orElse("Rookie");

                    // D-Day 계산 (Ongoing은 보통 업무 종료일 기준)
                    LocalDate endDate = post.getEndDate().toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(today, endDate);
                    String dDay = daysBetween == 0 ? "D-Day" :
                            daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

                    // Map에서 WORKING 멤버 수 꺼내오기 (없으면 0)
                    int currentWorkers = workingCountMap.getOrDefault(post.getId(), 0L).intValue();

                    return OngoingJobPostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .companyName(post.getCompanyName())
                            .count(post.getCount()) // 총 필요 인원 (applicantsTotal)
                            .applicantsCurrent(currentWorkers) // 현재 일하는 인원 (applicantsCurrent)
                            .dDay(dDay)
                            .employmentTag(employmentTag)
                            .employerStatus("ongoing")
                            .build();
                })
                .toList();
    }


    // [Employer] 완료된 공고 조회
    @Transactional(readOnly = true)
    public List<DoneJobPostResponse> getDonePosts(User employerUser) {

        // 1. DONE 상태인 공고 목록 조회 (1번의 쿼리)
        List<JobPost> posts = jobPostRepository.findDonePosts(employerUser.getId());

        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(JobPost::getId).toList();

        // 2. [최적화 🌟] 공고별 'DONE' 인원수 조회 (1번의 쿼리)
        List<Object[]> memberCounts = jobPostMemberRepository.countDoneMembersByPostIds(postIds);
        Map<Long, Long> memberCountMap = memberCounts.stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        // 3. [최적화 🌟] 내가 이 공고들에 쓴 리뷰 개수 조회 (1번의 쿼리)
        List<Object[]> reviewCounts = reviewRepository.countWrittenReviewsByPostIds(postIds, employerUser.getId());
        Map<Long, Long> reviewCountMap = reviewCounts.stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        LocalDate today = LocalDate.now();

        // 4. DTO 매핑 진행
        return posts.stream()
                .map(post -> {
                    String employmentTag = post.getJobPostTags().stream()
                            .filter(tag -> "EMPLOYMENT".equals(tag.getInterest().getType().name()))
                            .map(tag -> tag.getInterest().getName())
                            .findFirst()
                            .orElse("Rookie");

                    LocalDate endDate = post.getEndDate().toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(today, endDate);
                    String dDay = daysBetween == 0 ? "D-Day" :
                            daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

                    // 💡 비즈니스 로직 적용: 총원과 작성된 리뷰 수가 같으면 모두 리뷰한 것!
                    long totalWorkers = memberCountMap.getOrDefault(post.getId(), 0L);
                    long writtenReviews = reviewCountMap.getOrDefault(post.getId(), 0L);
                    boolean reviewedAll = totalWorkers > 0 && totalWorkers == writtenReviews;

                    return DoneJobPostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .companyName(post.getCompanyName())
                            .dDay(dDay)
                            .employmentTag(employmentTag)
                            .employerStatus("done")
                            .reviewedAll(reviewedAll) // 연산 결과 쏙 주입
                            .build();
                })
                .toList();
    }

    // [Employer] 지원자 전체 조회 API
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicants(
            User employer,
            Long postId
    ) {
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException("공고를 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("본인 공고만 조회 가능합니다.");
        }

// 수정: postId로 조회
        List<Application> applications =
                applicationRepository.findByJobPost_IdAndStatus(
                        postId,                      // ← job_post_id로 조회
                        Application.Status.APPLIED
                );

        return applications.stream()
                .map(app -> {

                    User user = app.getJobSeeker().getUser();

                    return ApplicationResponse.builder()
                            .applicationId(app.getId())
                            .userId(user.getId())
                            .name(user.getName())
                            .profileImage(
                                    user.getProfileImage() != null
                                            ? user.getProfileImage().getImageUrl()
                                            : null
                            )
                            .averageRating(user.getAverageRating())
                            .birthDate(user.getBirthDate())
                            .appliedAt(app.getAppliedAt())
                            .build();
                })
                .toList();

    }

    // [Employer] 지원자 수락 API
    @Transactional
    public void selectApplicants(
            User employer,
            Long postId,
            List<Long> applicationIds
    ) {
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException("공고를 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("본인 공고만 선발 가능합니다.");
        }

        if (applicationIds.size() != post.getCount()) {
            throw new IllegalArgumentException(
                    "모집 인원 수와 동일하게 선택해야 합니다."
            );
        }

        // APPLIED 상태 지원자만 조회
        List<Application> allApplications =
                applicationRepository.findByJobPost_IdAndStatus(
                        postId,
                        Application.Status.APPLIED
                );

        Set<Long> selectedIds = new HashSet<>(applicationIds);

        // 선택된 applicationId가 실제 존재하는지 검증
        long validCount = allApplications.stream()
                .filter(a -> selectedIds.contains(a.getId()))
                .count();

        if (validCount != applicationIds.size()) {
            throw new IllegalArgumentException(
                    "존재하지 않는 지원자가 포함되어 있습니다."
            );
        }

        for (Application application : allApplications) {

            if (selectedIds.contains(application.getId())) {

                application.setStatus(Application.Status.ACCEPTED);

                JobPostMember member = new JobPostMember();

                member.setJobPost(post);
                member.setEmployer(employer);
                member.setJobSeeker(application.getJobSeeker());
                member.setStatus(JobPostMember.Status.WORKING);

                jobPostMemberRepository.save(member);

            } else {

                applicationRepository.delete(application);
            }
        }
    }

    // [Employer] Done공고에 대해 리뷰 쓸 구직자들 목록 반환
    @Transactional(readOnly = true)
    public List<ReviewTargetResponse> getReviewTargets(User employer, Long postId) {
        // 1. 해당 공고의 멤버들 조회
        List<JobPostMember> members = jobPostMemberRepository.findByJobPostId(postId);

        // 2. 내가 쓴 리뷰 조회 (reviewer가 employer 본인인 것만)
        List<Review> myReviews = reviewRepository.findByJobPost_IdAndReviewer_Id(postId, employer.getId());

        // 3. 리뷰 작성 여부 판별용 Set 생성
        Set<Long> reviewedUserIds = myReviews.stream()
                .map(r -> r.getTargetUser().getId())
                .collect(Collectors.toSet());

        // 4. 멤버 목록을 순회하며 DTO로 변환
        return members.stream()
                .filter(member -> member.getJobSeeker() != null && member.getJobSeeker().getUser() != null) // 🌟 안전 장치 추가
                .map(member -> {
                    User targetUser = member.getJobSeeker().getUser();

            // 프로필 이미지 URL 안전하게 가져오기
            String profileImageUrl = null;
            if (targetUser.getProfileImage() != null) {
                profileImageUrl = targetUser.getProfileImage().getImageUrl();
            }

            // 5. Set에 targetUser의 ID가 포함되어 있다면 이미 리뷰를 쓴 것 (isReviewed = true)
            boolean isReviewed = reviewedUserIds.contains(targetUser.getId());

            return ReviewTargetResponse.builder()
                    .targetUserId(targetUser.getId())
                    .name(targetUser.getName())
                    .profileImage(profileImageUrl)
                    .isReviewed(isReviewed) // 🌟 프론트에서 이 값을 보고 '작성 완료' 처리
                    .build();

        }).toList();
    }

    // [Employer] 구직자에게 리뷰 달기
    @Transactional
    public void createReview(User employer, Long postId, ReviewRequest request) {
        // 1. 공고 검증
        JobPost post = jobPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("본인 공고에 대한 리뷰만 작성 가능합니다.");
        }

        // 2. 리뷰 중복 방지
        if (reviewRepository.existsByJobPost_IdAndReviewer_IdAndTargetUser_Id(
                postId, employer.getId(), request.getTargetUserId())) {
            throw new IllegalStateException("이미 이 구직자에게 리뷰를 작성하셨습니다.");
        }

        // 3. 리뷰 대상자 찾기
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new IllegalArgumentException("리뷰 대상자를 찾을 수 없습니다."));

        // 4. 리뷰 저장
        Review review = new Review();
        review.setJobPost(post);
        review.setReviewer(employer);
        review.setTargetUser(targetUser);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        // 5. 🌟 평균 별점 갱신 (리뷰 저장 후 호출)
        updateAverageRating(targetUser);
    }

    // 별점 업데이트 전용 private 메서드
    private void updateAverageRating(User targetUser) {
        Double average = reviewRepository.calculateAverageRatingByTargetUserId(targetUser.getId());

        // Double 타입에 맞춰 그대로 세팅 (만약 리뷰가 아예 없다면 초기값인 0.0 처리)
        Double newRating = (average != null) ? average : 0.0;

        targetUser.setAverageRating(newRating);
    }
}
