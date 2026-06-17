package com.growvy.repository;

import com.growvy.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    boolean existsByJobPost_IdAndJobSeeker_User_IdAndStatus(
            Long jobPostId,
            Long userId,
            Note.Status status
    );
}
