package com.growvy.service;

import com.growvy.dto.req.EmployerSignUpRequest;
import com.growvy.dto.req.JobSeekerSignUpRequest;
import com.growvy.dto.res.AuthResponse;
import com.growvy.entity.*;
import com.growvy.repository.*;
import com.growvy.util.JwtUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
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


    public AuthResponse login(String firebaseIdToken) throws FirebaseAuthException {
        FirebaseToken decoded;
        try {
            decoded = FirebaseAuth.getInstance().verifyIdToken(firebaseIdToken);
            System.out.println("Firebase ID Token verified. UID: " + decoded.getUid());
        } catch (FirebaseAuthException e) {
            System.err.println("Firebase ID Token verification failed!");
            System.err.println("Token: " + firebaseIdToken); // 확인용
            e.printStackTrace();
            throw e;
        }

        String firebaseUid = decoded.getUid();

        var userOpt = userRepository.findByFirebaseUid(firebaseUid);

        boolean registered = userOpt.isPresent(); // 최초 로그인: false, 재 로그인: true
        Long userId = userOpt.map(User::getId).orElse(null);

        String jwt = jwtProvider.createToken(firebaseUid, userId);

        System.out.println("JWT created: " + jwt + " | Registered: " + registered);

        return new AuthResponse(jwt, registered);
    }


    @Transactional
    public void employerSignUp(String jwt, EmployerSignUpRequest req) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        if (userRepository.findByFirebaseUid(firebaseUid).isPresent()) {
            throw new IllegalStateException("이미 가입된 사용자입니다.");
        }

        Image profileImage = null;
        Image bannerImage = null;

        if (req.getProfileImageId() != null) {
            profileImage = imageRepository.findById(req.getProfileImageId())
                    .orElseThrow(() -> new IllegalArgumentException("프로필 이미지 존재하지 않음"));
        }

        if (req.getBannerImageId() != null) {
            bannerImage = imageRepository.findById(req.getBannerImageId())
                    .orElseThrow(() -> new IllegalArgumentException("배너 이미지 존재하지 않음"));
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
        user.setBannerImage(bannerImage);

        userRepository.save(user);

        // 2. EmployerProfile 생성
        EmployerProfile employerProfile = new EmployerProfile();
        employerProfile.setUser(user);
        employerProfile.setCompanyName(req.getCompanyName());
        employerProfile.setBusinessAddress(req.getBusinessAddress());

        // GeoService에서 좌표 + state, city 가져오기
        Map<String, Object> locationInfo = geoService.getCoordinates(req.getBusinessAddress());
        if (locationInfo == null || locationInfo.get("lat") == null || locationInfo.get("lng") == null) {
            throw new IllegalStateException("사업장 주소 좌표 변환 실패");
        }

        employerProfile.setLat((Double) locationInfo.get("lat"));
        employerProfile.setLng((Double) locationInfo.get("lng"));
        employerProfile.setState(locationInfo.getOrDefault("state", "").toString());
        employerProfile.setCity(locationInfo.getOrDefault("city", "").toString());

        log.info("EmployerProfile 위치 설정: lat={}, lng={}, state={}, city={}",
                locationInfo.get("lat"), locationInfo.get("lng"),
                locationInfo.getOrDefault("state", ""), locationInfo.getOrDefault("city", ""));

        employerProfileRepository.save(employerProfile);
    }

    @Transactional
    public void jobseekerSignUp(String jwt, JobSeekerSignUpRequest req) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        if (userRepository.findByFirebaseUid(firebaseUid).isPresent()) {
            throw new IllegalStateException("이미 가입된 사용자입니다.");
        }

        Image profileImage = null;
        Image bannerImage = null;

        if (req.getProfileImageId() != null) {
            profileImage = imageRepository.findById(req.getProfileImageId())
                    .orElseThrow(() -> new IllegalArgumentException("프로필 이미지 존재하지 않음"));
        }

        if (req.getBannerImageId() != null) {
            bannerImage = imageRepository.findById(req.getBannerImageId())
                    .orElseThrow(() -> new IllegalArgumentException("배너 이미지 존재하지 않음"));
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
        user.setBannerImage(bannerImage);

        userRepository.save(user);

        // 2. JobSeekerProfile 생성
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        jobSeekerProfile.setUser(user);
        jobSeekerProfile.setHomeAddress(req.getHomeAddress());
        jobSeekerProfile.setCareer(req.getCareer());
        jobSeekerProfile.setBio(req.getBio());

        // GeoService에서 좌표 + state, city 가져오기
        Map<String, Object> locationInfo = geoService.getCoordinates(req.getHomeAddress());
        if (locationInfo == null || locationInfo.get("lat") == null || locationInfo.get("lng") == null) {
            throw new IllegalStateException("주소 좌표 변환 실패");
        }

        jobSeekerProfile.setLat((Double) locationInfo.get("lat"));
        jobSeekerProfile.setLng((Double) locationInfo.get("lng"));
        jobSeekerProfile.setState(locationInfo.getOrDefault("state", "").toString());
        jobSeekerProfile.setCity(locationInfo.getOrDefault("city", "").toString());

        log.info("JobSeekerProfile 위치 설정: lat={}, lng={}, state={}, city={}",
                locationInfo.get("lat"), locationInfo.get("lng"),
                locationInfo.getOrDefault("state", ""), locationInfo.getOrDefault("city", ""));

        jobSeekerProfileRepository.save(jobSeekerProfile);

        // 3. Interest 연결
        if (req.getInterestIds() != null && !req.getInterestIds().isEmpty()) {
            for (Long interestId : req.getInterestIds()) {
                Interest interest = interestRepository.findById(interestId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 interest ID: " + interestId));

                JobSeekerInterest jsi = new JobSeekerInterest();
                jsi.setJobSeekerProfile(jobSeekerProfile);
                jsi.setInterest(interest);
                jsi.setId(new JobSeekerInterestId(jobSeekerProfile.getUser().getId(), interest.getId()));

                jobSeekerInterestRepository.save(jsi);
            }
        }
    }

    // User 조회 메서드
    public User getUserByJwt(String jwt) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // JobSeekerProfile 조회
    public JobSeekerProfile getJobSeekerProfileByJwt(String jwt) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        return jobSeekerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("구직자 프로필 없음"));
    }
}
