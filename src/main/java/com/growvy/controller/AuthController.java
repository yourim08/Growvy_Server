package com.growvy.controller;

import com.growvy.dto.req.EmployerSignUpRequest;
import com.growvy.dto.req.JobSeekerSignUpRequest;
import com.growvy.dto.res.AuthResponse;
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

    @Operation(summary = "로그인 API", description = "사용자의 firebaseUid를 조회해 accessToken과 회원가입 여부를 반환")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestHeader("Authorization") String header
    ) throws FirebaseAuthException {

        String firebaseToken = header.replace("Bearer ", "");

        return ResponseEntity.ok(authService.login(firebaseToken));
    }

    @Operation(summary = "Employer 회원가입 API", description = "Employer 역할의 회원가입을 진행")
    @PostMapping("/signup/employer")
    public ResponseEntity<SignUpResponse> employerSignup(
            @RequestHeader("Authorization") String header,
            @RequestBody EmployerSignUpRequest request
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        authService.employerSignUp(jwt, request);
        return ResponseEntity.ok(new SignUpResponse(true));
    }

    @Operation(summary = "JobSeeker 회원가입 API", description = "JobSeeker 역할의 회원가입을 진행")
    @PostMapping("/signup/jobseeker")
    public ResponseEntity<SignUpResponse> jobseekerSignup(
            @RequestHeader("Authorization") String header,
            @RequestBody JobSeekerSignUpRequest request
    ) {
        String jwt = header.replace("Bearer ", "").trim();
        authService.jobseekerSignUp(jwt, request);
        return ResponseEntity.ok(new SignUpResponse(true));
    }
}

