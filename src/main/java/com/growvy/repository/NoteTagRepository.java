package com.growvy.repository;

import com.growvy.entity.NoteTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteTagRepository
        extends JpaRepository<NoteTag, Long> {
}