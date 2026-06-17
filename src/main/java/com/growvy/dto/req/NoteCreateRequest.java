package com.growvy.dto.req;

import com.growvy.entity.Note;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NoteCreateRequest {

    private Long jobPostId;

    private String title;

    private String content;

    private Note.OverallExperience overallExperience;

    // 선택된 태그들
    private List<Long> learnTagIds;

    // 사용자가 직접 입력한 태그들
    private List<String> customTags;

}

