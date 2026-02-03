package com.growvy.service;

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

@Service
@RequiredArgsConstructor
public class JobPostSearchService {

    private final JobPostRepository jobPostRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    @Transactional
    public List<JobPost> searchJobPosts(User user, String keyword, String state, String city) {
        // 1. 검색 기록 저장
        JobSeekerProfile jobSeekerProfile = user.getJobSeekerProfile();
        if (jobSeekerProfile != null) { // profile이 존재하면 기록 저장
            SearchHistory history = new SearchHistory();
            history.setUser(user);
            history.setKeyword(keyword);
            searchHistoryRepository.save(history);
        }

        // 2. 검색 수행
        return jobPostRepository.searchByKeywordAndLocation(keyword, state, city);
    }
}
