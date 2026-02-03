package com.growvy.repository;

import com.growvy.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}

