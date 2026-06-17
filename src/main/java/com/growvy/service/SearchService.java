package com.growvy.service;


import com.growvy.entity.Interest;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobPostImage;
import com.growvy.entity.SearchHistory;
import com.growvy.entity.User;
import com.growvy.dto.res.JobSeekerJobPostResponse;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final JobPostRepository jobPostRepository;
    private final SearchHistoryRepository searchHistoryRepository;


    @Transactional // 🌟 검색 기록 저장을 위해 필수
    public List<JobSeekerJobPostResponse> searchJobPosts(User user, String keyword) {

        String trimmedKeyword = (keyword != null) ? keyword.trim() : "";

        if (trimmedKeyword.isEmpty()) {
            return Collections.emptyList(); // 검색어가 없으면 빈 배열 반환
        }

        // 1. 검색 기록 저장
        SearchHistory history = new SearchHistory();
        history.setUser(user);
        history.setKeyword(trimmedKeyword);
        searchHistoryRepository.save(history);

        // 2. 공고 검색 (제목 기준)
        List<JobPost> posts = jobPostRepository.searchJobPostsExcludingApplied(trimmedKeyword, user.getId());
        LocalDate today = LocalDate.now();

        // 3. 기존 리스트 조회와 완벽하게 동일한 로직으로 DTO 매핑
        return posts.stream()
                .map(post -> {
                    // [태그 추출]
                    List<String> employmentTags = post.getJobPostTags().stream()
                            .filter(tag -> tag.getInterest().getType() == Interest.InterestType.EMPLOYMENT)
                            .map(tag -> tag.getInterest().getName())
                            .toList();

                    // [이미지 추출]
                    List<String> imageUrls = post.getImages().stream()
                            .map(JobPostImage::getImageUrl)
                            .toList();

                    // [D-Day 계산]
                    LocalDate deadlineDate = post.getRecruitmentDeadline().toLocalDate();
                    long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);
                    String dDay = daysBetween == 0 ? "D-Day" :
                            daysBetween > 0 ? "D-" + daysBetween : "D+" + Math.abs(daysBetween);

                    // 4. 기존과 동일한 DTO에 담아서 반환
                    return JobSeekerJobPostResponse.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .companyName(post.getCompanyName()) // 🌟 수정됨
                            .endDate(post.getEndDate())
                            .tags(employmentTags) // 🌟 수정됨
                            .dDay(dDay) // 🌟 수정됨
                            .hourlyWage(post.getHourlyRates()) // 🌟 수정됨
                            .description(post.getDescription()) // 🌟 수정됨
                            .imageUrls(imageUrls) // 🌟 수정됨
                            .build();
                })
                .toList();
    }
}
