package com.growvy.repository;

import com.growvy.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {
    // 필요시 findByApplicationId 등 추가 가능
}
