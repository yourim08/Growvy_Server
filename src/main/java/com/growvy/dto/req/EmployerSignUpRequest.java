package com.growvy.dto.req;

import com.growvy.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployerSignUpRequest {

    // User
    private String name;
    private String email;
    private LocalDate birthDate;
    private User.Gender gender;
    private String phone;
    private Long profileImageId;

    // EmployerProfile
    private String companyName;
    private String businessAddress;

    public enum Gender {
        MALE,
        FEMALE
    }
}
