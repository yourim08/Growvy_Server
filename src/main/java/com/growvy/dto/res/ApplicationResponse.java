package com.growvy.dto.res;

import com.growvy.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationResponse {
    private Long applicationId;
    private String status;
    private String name;
    private User.Gender gender;
    private Double averageRating;
}
