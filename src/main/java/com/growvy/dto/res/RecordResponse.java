package com.growvy.dto.res;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecordResponse {
    private Long applicationId;
    private String title;
    private String content;
    private Boolean isCompleted;
    private List<String> imageUrls;
}
