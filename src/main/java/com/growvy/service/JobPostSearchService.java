package com.growvy.service;

import com.growvy.dto.res.JobPostResponse;
import com.growvy.entity.JobPost;
import com.growvy.entity.JobSeekerProfile;
import com.growvy.entity.SearchHistory;
import com.growvy.entity.User;
import com.growvy.repository.JobPostRepository;
import com.growvy.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostSearchService {

    private final JobPostRepository jobPostRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    @Transactional
    public List<JobPostResponse> searchJobPosts(User user, String keyword, String state, String city) {
        // 1. 검색 기록 저장
        JobSeekerProfile jobSeekerProfile = user.getJobSeekerProfile();
        if (jobSeekerProfile != null) { // profile이 존재하면 기록 저장
            SearchHistory history = new SearchHistory();
            history.setUser(user);
            history.setKeyword(keyword);
            searchHistoryRepository.save(history);
        }

        // 2. 검색 수행
        List<JobPost> results = jobPostRepository.searchByKeywordAndLocation(keyword, state, city);

        // 3. DTO 변환
        return results.stream().map(jobPost -> {
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
            res.setState(jobPost.getState());
            res.setCity(jobPost.getCity());
            res.setCreatedAt(jobPost.getCreatedAt());
            res.setStatus(jobPost.getStatus().name());
            res.setTags(jobPost.getJobPostTags().stream()
                    .map(tag -> tag.getInterest().getName())
                    .toList());
            res.setImageUrls(jobPost.getJobPostImages().stream()
                    .map(img -> img.getImageUrl())
                    .toList());
            return res;
        }).collect(Collectors.toList());
    }
}
