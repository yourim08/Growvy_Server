package com.growvy.controller;

import com.growvy.dto.req.EmployerProfileUpdateRequest;
import com.growvy.dto.req.EmployerSignUpRequest;
import com.growvy.dto.req.JobSeekerSignUpRequest;
import com.growvy.dto.req.JobSeekerProfileUpdateRequest;
import com.growvy.dto.res.AuthResponse;
import com.growvy.dto.res.IsEmployerResponse;
import com.growvy.dto.res.SignUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.growvy.service.AuthService;

import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "[공통] 로그인", description = "사용자의 firebaseUid를 조회해 accessToken과 회원가입 여부를 반환")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestHeader("Authorization") String header
    ) throws FirebaseAuthException {

        String firebaseToken = header.replace("Bearer ", "");

        return ResponseEntity.ok(authService.login(firebaseToken));
    }

    @Operation(summary = "[Employer] 회원가입", description = "Employer 역할의 회원가입을 진행")
    @PostMapping("/signup/employer")
    public ResponseEntity<SignUpResponse> employerSignup(
            @RequestHeader("Authorization") String header,
            @RequestBody EmployerSignUpRequest request
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        authService.employerSignUp(jwt, request);
        return ResponseEntity.ok(new SignUpResponse(true));
    }

    @Operation(summary = "[JobSeeker] 회원가입", description = "JobSeeker 역할의 회원가입을 진행")
    @PostMapping("/signup/jobseeker")
    public ResponseEntity<SignUpResponse> jobseekerSignup(
            @RequestHeader("Authorization") String header,
            @RequestBody JobSeekerSignUpRequest request
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        authService.jobseekerSignUp(jwt, request);
        return ResponseEntity.ok(new SignUpResponse(true));
    }

    @Operation(summary = "[공통] 역할 조회", description = "JobSeeker / Employer")
    @GetMapping("/is-employer")
    public IsEmployerResponse isEmployer(
            @RequestHeader("Authorization") String authorization
    ) {
        String jwt = authorization.replace("Bearer ", "");
        return authService.isEmployer(jwt);
    }

    @Operation(summary = "[JobSeeker] 프로필 수정", description = "JobSeeker 역할의 정보 수정")
    @PutMapping("/jobseeker/profile")
    public ResponseEntity<SignUpResponse> updateJobSeekerProfile(
            @RequestHeader("Authorization") String header,
            @RequestBody JobSeekerProfileUpdateRequest req
    ) {
        String jwt = header.replace("Bearer ", "").trim();

        authService.updateJobSeekerProfile(jwt, req);

        return ResponseEntity.ok(new SignUpResponse(true));
    }

    @Operation(summary = "[Employer] 프로필 수정", description = "Employer 역할의 정보 수정")
    @PutMapping("/employer/profile")
    public ResponseEntity<SignUpResponse> updateEmployerProfile(
            @RequestHeader("Authorization") String header,
            @RequestBody EmployerProfileUpdateRequest req
    ) {
        String jwt = header.replace("Bearer ", "").trim();

        authService.updateEmployerProfile(jwt, req);

        return ResponseEntity.ok(new SignUpResponse(true));
    }
}

