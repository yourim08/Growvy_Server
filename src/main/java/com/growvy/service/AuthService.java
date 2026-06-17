package com.growvy.service;

import com.growvy.dto.req.EmployerProfileUpdateRequest;
import com.growvy.dto.req.EmployerSignUpRequest;
import com.growvy.dto.req.JobSeekerSignUpRequest;
import com.growvy.dto.req.JobSeekerProfileUpdateRequest;
import com.growvy.dto.res.AuthResponse;
import com.growvy.dto.res.IsEmployerResponse;
import com.growvy.entity.*;
import com.growvy.repository.*;
import com.growvy.util.JwtUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final JobSeekerInterestRepository jobSeekerInterestRepository;
    private final InterestRepository interestRepository;
    private final JwtUtil jwtProvider;
    private final GeoService geoService;

    // [공통] 로그인 (이곳만 순수 Firebase 토큰을 검사합니다)
    public AuthResponse login(String firebaseIdToken) throws FirebaseAuthException {
        FirebaseToken decoded;
        try {
            decoded = FirebaseAuth.getInstance().verifyIdToken(firebaseIdToken);
            System.out.println("Firebase ID Token verified. UID: " + decoded.getUid());
        } catch (FirebaseAuthException e) {
            System.err.println("Firebase ID Token verification failed!");
            System.err.println("Token: " + firebaseIdToken);
            e.printStackTrace();
            throw e;
        }

        String firebaseUid = decoded.getUid();
        var userOpt = userRepository.findByFirebaseUid(firebaseUid);

        boolean registered = userOpt.isPresent();
        Long userId = userOpt.map(User::getId).orElse(null);
        String jwt = jwtProvider.createToken(firebaseUid, userId);

        System.out.println("JWT created: " + jwt + " | Registered: " + registered);
        return new AuthResponse(jwt, registered);
    }

    // [Employer] 회원가입
    @Transactional
    public void employerSignUp(String firebaseToken, EmployerSignUpRequest req) {
        String firebaseUid = jwtProvider.getFirebaseUid(firebaseToken);
        if (userRepository.findByFirebaseUid(firebaseUid).isPresent()) {
            throw new IllegalStateException("이미 가입된 사용자입니다.");
        }

        Image profileImage = null;

        if (req.getProfileImageId() != null) {
            profileImage = imageRepository.findById(req.getProfileImageId())
                    .orElseThrow(() -> new IllegalArgumentException("프로필 이미지 존재하지 않음"));
        }

        // 1. User 생성
        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setBirthDate(req.getBirthDate());
        user.setGender(req.getGender());
        user.setPhone(req.getPhone());
        user.setProfileImage(profileImage);
        userRepository.save(user);

        // 2. EmployerProfile 생성
        EmployerProfile employerProfile = new EmployerProfile();
        employerProfile.setUser(user);
        employerProfile.setCompanyName(req.getCompanyName());
        employerProfile.setBusinessAddress(req.getBusinessAddress());

        employerProfileRepository.save(employerProfile);
    }

    // [JobSeeker] 회원가입
    @Transactional
    public void jobseekerSignUp(String firebaseToken, JobSeekerSignUpRequest req) {
        String firebaseUid = jwtProvider.getFirebaseUid(firebaseToken);

        if (userRepository.findByFirebaseUid(firebaseUid).isPresent()) {
            throw new IllegalStateException("이미 가입된 사용자입니다.");
        }

        Image profileImage = null;
        if (req.getProfileImageId() != null) {
            profileImage = imageRepository.findById(req.getProfileImageId())
                    .orElseThrow(() -> new IllegalArgumentException("프로필 이미지 존재하지 않음"));
        }

        // 1. User 생성
        User user = new User();
        user.setFirebaseUid(firebaseUid);
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setBirthDate(req.getBirthDate());
        user.setGender(User.Gender.valueOf(req.getGender().name()));
        user.setPhone(req.getPhone());
        user.setProfileImage(profileImage);
        userRepository.save(user);

        // 2. JobSeekerProfile 생성
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        jobSeekerProfile.setUser(user);
        jobSeekerProfile.setHomeAddress(req.getHomeAddress());
        jobSeekerProfile.setCareer(req.getCareer());
        jobSeekerProfile.setBio(req.getBio());

        jobSeekerProfileRepository.save(jobSeekerProfile);

        // 3. 관심사DB와 연결
        if (req.getInterestIds() != null && !req.getInterestIds().isEmpty()) {
            for (Long interestId : req.getInterestIds()) {
                Interest interest = interestRepository.findById(interestId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 interest ID: " + interestId));

                JobSeekerInterest jsi = new JobSeekerInterest();
                jsi.setJobSeekerProfile(jobSeekerProfile);
                jsi.setInterest(interest);
                jsi.setId(new JobSeekerInterestId(user.getId(), interest.getId()));

                jobSeekerInterestRepository.save(jsi);
            }
        }
    }

    // [JobSeeker] 관심사 가중치 부여 API



    // 역할 조회
    public IsEmployerResponse isEmployer(String jwt) {
        // 에러의 주범이었던 곳 수정 (백엔드 자체 토큰 파서 사용)
        String firebaseUid = jwtProvider.getFirebaseUidFromBackendToken(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElse(null);

        if (user == null) {
            return new IsEmployerResponse(false);
        }

        boolean isEmployer = employerProfileRepository.existsById(user.getId());
        return new IsEmployerResponse(isEmployer);
    }

    // User 조회
    public User getUserByJwt(String jwt) {
        // 백엔드 자체 토큰 파서 사용
        String firebaseUid = jwtProvider.getFirebaseUidFromBackendToken(jwt);
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public JobSeekerProfile getJobSeekerProfileByJwt(String jwt) {
        // 백엔드 자체 토큰 파서 사용
        String firebaseUid = jwtProvider.getFirebaseUidFromBackendToken(jwt);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        return jobSeekerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("구직자 프로필 없음"));
    }


    // 주소 파싱: city / state
    private AddressInfo parseCityAndState(String address) {
        if (address == null || address.isBlank()) {
            return new AddressInfo(null, null);
        }

        String[] parts = address.split(",");
        String city = parts[1].trim();   // 항상 두 번째 요소
        String state = parts[2].trim();  // 항상 세 번째 요소

        return new AddressInfo(city, state);
    }

    // JobSeeker 프로필 수정
    @Transactional
    public void updateJobSeekerProfile(String jwt, JobSeekerProfileUpdateRequest req) {
        // 백엔드 자체 토큰 파서 사용
        String firebaseUid = jwtProvider.getFirebaseUidFromBackendToken(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        JobSeekerProfile profile = jobSeekerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("구직자 프로필 없음"));

        // 1. User 수정
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());

        if (req.getGender() != null) {
            user.setGender(User.Gender.valueOf(req.getGender().name()));
        }

        if (req.getProfileImageId() != null) {
            Image profileImage = imageRepository.findById(req.getProfileImageId())
                    .orElseThrow(() -> new IllegalArgumentException("프로필 이미지 존재하지 않음"));

            user.setProfileImage(profileImage);
        }

        userRepository.save(user);

        // 2. JobSeekerProfile 수정
        profile.setCareer(req.getCareer());
        profile.setBio(req.getBio());

        jobSeekerProfileRepository.save(profile);

        // 3. 관심사 전체 교체
        if (req.getInterestIds() != null) {

            jobSeekerInterestRepository.deleteByJobSeekerProfile(profile);

            for (Long interestId : req.getInterestIds()) {

                Interest interest = interestRepository.findById(interestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("존재하지 않는 interest ID: " + interestId));

                JobSeekerInterest jsi = new JobSeekerInterest();

                jsi.setJobSeekerProfile(profile);
                jsi.setInterest(interest);
                jsi.setId(new JobSeekerInterestId(user.getId(), interest.getId()));

                jobSeekerInterestRepository.save(jsi);
            }
        }
    }


    // Employer 프로필 수정
    @Transactional
    public void updateEmployerProfile(String jwt, EmployerProfileUpdateRequest req) {
        // 백엔드 자체 토큰 파서 사용
        String firebaseUid = jwtProvider.getFirebaseUidFromBackendToken(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        EmployerProfile profile = employerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("구인자 프로필 없음"));

        // 1. User 수정
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());

        if (req.getGender() != null) {
            user.setGender(User.Gender.valueOf(req.getGender().name()));
        }

        if (req.getProfileImageId() != null) {
            Image profileImage = imageRepository.findById(req.getProfileImageId())
                    .orElseThrow(() -> new IllegalArgumentException("프로필 이미지 존재하지 않음"));

            user.setProfileImage(profileImage);
        }

        userRepository.save(user);

        // 2. EmployerProfile 수정
        profile.setCompanyName(req.getCompanyName());


        employerProfileRepository.save(profile);
    }


    @Getter
    @AllArgsConstructor
    private static class AddressInfo {
        private final String city;
        private final String state;
    }
}