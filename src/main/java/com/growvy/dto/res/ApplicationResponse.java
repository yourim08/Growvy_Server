package com.growvy.dto.res;

import com.growvy.entity.User;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long applicationId;
    private Long userId;
    private String name;
    private String profileImage;
    private Double averageRating;
    private LocalDate birthDate;
    private LocalDateTime appliedAt;
}
