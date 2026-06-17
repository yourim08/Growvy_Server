package com.growvy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "note_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"note_id", "learn_tag_id"}
                )
        }
)
@Getter
@Setter
public class NoteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 노트
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    // 태그
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learn_tag_id", nullable = false)
    private LearnTag learnTag;
}