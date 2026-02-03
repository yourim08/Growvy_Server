package com.growvy.dto.req;

import com.growvy.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class JobSeekerSignUpRequest {

    // User
    private String name;
    private String email;
    private LocalDate birthDate;
    private Gender gender;
    private String phone;
    private Long profileImageId;
    private Long bannerImageId;

    // JobSeekerProfile
    private String homeAddress;
    private String career;
    private String bio;

    // Interest
    private List<Long> interestIds;

    public enum Gender {
        MALE,
        FEMALE
    }
}
