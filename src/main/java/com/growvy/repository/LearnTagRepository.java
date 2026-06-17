package com.growvy.repository;

import com.growvy.entity.LearnTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearnTagRepository
        extends JpaRepository<LearnTag, Long> {

    Optional<LearnTag> findByName(String name);
}