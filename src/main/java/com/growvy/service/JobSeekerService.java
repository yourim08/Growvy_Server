package com.growvy.service;

import com.growvy.dto.req.JobPostRequest;
import com.growvy.dto.res.JobPostResponse;
import com.growvy.dto.res.JobSeekerJobPostResponse;
import com.growvy.entity.*;
import com.growvy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerService {

    private final ApplicationRepository applicationRepository;
    private final JobPostRepository jobPostRepository;
    private final JobPostMemberRepository jobPostMemberRepository;
    private final NoteRepository noteRepository;


    // [JobSeeker] 신청한 일 조회 - APPLIED
    @Transactional(readOnly = true)
    public List<JobSeekerJobPostResponse> getAppliedJobPosts(User user) { // 💡 인자를 User 객체로 변경!

        // 💡 레포지토리를 호출할 때 user.getId()를 넘겨줍니다.
        List<Application> applications = applicationRepository.findAppliedPostsByApplicationDate(user.getId(), Application.Status.APPLIED);
        LocalDate today = LocalDate.now();
        // 2. 조회된 지원서 리스트를 순회하며 DTO 변환
        return applications.stream()
                .map(application -> {
                    JobPost post = application.getJobPost();

                    // [태그 추출]
                    List<String> employmentTags = post.getJobPostTags().stream()
                            .filter(tag -> tag.getInterest().getType() == Interest.InterestType.EMPLOYMENT)
                            .map(tag -> tag.getInterest().getName())
                            .collect(Collectors.toList());

                    // [이미지 추출]
                    List<String> imageUrls = post.getImages().stream()
                            .map(JobPostImage::getImageUrl)
                            .toList();

                    LocalDate deadlineDate = post.getRecruitmentDeadline().toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);
                    String dDay = daysBetween == 0 ? "D-Day" :
                            daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

                    // 3. 빌더 패턴 조립
                    return JobSeekerJobPostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .companyName(post.getCompanyName())
                            .endDate(post.getEndDate())
                            .tags(employmentTags)
                            .dDay(dDay)
                            .hourlyWage(post.getHourlyRates())
                            .description(post.getDescription())
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());
    }


    // [JobSeeker] 구직자 Ongoing 일 조회
    @Transactional(readOnly = true)
    public List<JobSeekerJobPostResponse> getOngoingJobPosts(User user) {

        // 1. 구직자 ID로 'WORKING' 상태인 멤버십 내역을 모두 조회합니다.
        List<JobPostMember> workingMembers = jobPostMemberRepository.findByJobSeeker_UserIdAndStatus(user.getId(), JobPostMember.Status.WORKING);
        LocalDate today = LocalDate.now();

        // 2. 조회된 리스트를 순회하며 공통 DTO로 변환
        return workingMembers.stream()
                .map(member -> {
                    // JobPostMember 내부의 공고(JobPost) 객체를 꺼냅니다.
                    JobPost post = member.getJobPost();

                    // [태그 추출]
                    List<String> employmentTags = post.getJobPostTags().stream()
                            .filter(tag -> tag.getInterest().getType() == Interest.InterestType.EMPLOYMENT)
                            .map(tag -> tag.getInterest().getName())
                            .collect(Collectors.toList());

                    // [이미지 추출]
                    List<String> imageUrls = post.getImages().stream()
                            .map(JobPostImage::getImageUrl)
                            .toList();


                    LocalDate deadlineDate = post.getRecruitmentDeadline().toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);
                    String dDay = daysBetween == 0 ? "D-Day" :
                            daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

                    // 3. 기존과 동일한 DTO에 담아서 반환! (재사용의 마법 ✨)
                    return JobSeekerJobPostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .companyName(post.getCompanyName())
                            .endDate(post.getEndDate())
                            .tags(employmentTags)
                            .dDay(dDay)
                            .hourlyWage(post.getHourlyRates())
                            .description(post.getDescription())
                            .imageUrls(imageUrls)
                            .build();
                })
                .collect(Collectors.toList());
    }


    // [JobSeeker] Done 상태 일 조회
    @Transactional(readOnly = true)
    public List<JobSeekerJobPostResponse> getDoneJobPosts(User user) {

        // 1. 구직자 ID로 'DONE' 상태인 멤버십 내역을 모두 조회합니다.
        List<JobPostMember> doneMembers = jobPostMemberRepository.findByJobSeeker_UserIdAndStatus(user.getId(), JobPostMember.Status.DONE);
        LocalDate today = LocalDate.now();

        // 2. 조회된 리스트를 순회하며 공통 DTO로 변환
        return doneMembers.stream()
                .map(member -> {
                    JobPost post = member.getJobPost();

                    // [태그 추출] 최신 문법(.toList()) 적용!
                    List<String> employmentTags = post.getJobPostTags().stream()
                            .filter(tag -> tag.getInterest().getType() == Interest.InterestType.EMPLOYMENT)
                            .map(tag -> tag.getInterest().getName())
                            .toList();

                    // [이미지 추출]
                    List<String> imageUrls = post.getImages().stream()
                            .map(JobPostImage::getImageUrl)
                            .toList();
                    LocalDate deadlineDate = post.getRecruitmentDeadline().toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);
                    String dDay = daysBetween == 0 ? "D-Day" :
                            daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

                    // 3. 기존과 동일한 DTO에 담아서 반환
                    return JobSeekerJobPostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .companyName(post.getCompanyName())
                            .endDate(post.getEndDate())
                            .tags(employmentTags)
                            .dDay(dDay)
                            .hourlyWage(post.getHourlyRates())
                            .description(post.getDescription())
                            .imageUrls(imageUrls)
                            .build();
                })
                .toList();
    }


    // [JobSeeker] 공고 지원
    @Transactional
    public void applyJobPost(
            User user,
            Long postId
    ) {
        JobSeekerProfile jobSeeker = user.getJobSeekerProfile();

        if (jobSeeker == null) {
            throw new IllegalArgumentException("구직자 프로필이 없습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException("공고를 찾을 수 없습니다."));

        // 이미 지원한 공고인지 확인
        boolean alreadyApplied =
                applicationRepository.existsByJobPostAndJobSeeker(
                        jobPost,
                        jobSeeker
                );

        if (alreadyApplied) {
            throw new IllegalArgumentException("이미 지원한 공고입니다.");
        }

        Application application = new Application();

        application.setJobPost(jobPost);
        application.setJobSeeker(jobSeeker);
        application.setStatus(Application.Status.APPLIED);
        application.setAppliedAt(LocalDateTime.now());

        applicationRepository.save(application);
    }
}

