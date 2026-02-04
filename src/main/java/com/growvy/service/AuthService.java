package com.growvy.service;

import com.growvy.dto.req.EmployerSignUpRequest;
import com.growvy.dto.req.JobSeekerSignUpRequest;
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

    // 로그인
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

    // 구인자 회원가입
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

        // 좌표
        Map<String, Double> coords = geoService.getCoordinates(req.getBusinessAddress());
        if (coords == null || coords.get("lat") == null || coords.get("lng") == null) {
            throw new IllegalStateException("사업장 주소 좌표 변환 실패");
        }
        employerProfile.setLat(coords.get("lat"));
        employerProfile.setLng(coords.get("lng"));

        // city / state 파싱
        AddressInfo addrInfo = parseCityAndState(req.getBusinessAddress());
        employerProfile.setCity(addrInfo.getCity());
        employerProfile.setState(addrInfo.getState());

        log.info("EmployerProfile 저장: city={}, state={}, lat={}, lng={}",
                addrInfo.getCity(), addrInfo.getState(), coords.get("lat"), coords.get("lng"));

        employerProfileRepository.save(employerProfile);
    }

    // 구직자 회원가입
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

        // 좌표
        Map<String, Double> coords = geoService.getCoordinates(req.getHomeAddress());
        if (coords == null || coords.get("lat") == null || coords.get("lng") == null) {
            throw new IllegalStateException("주소 좌표 변환 실패");
        }
        jobSeekerProfile.setLat(coords.get("lat"));
        jobSeekerProfile.setLng(coords.get("lng"));

        // city / state 파싱
        AddressInfo addrInfo = parseCityAndState(req.getHomeAddress());
        jobSeekerProfile.setCity(addrInfo.getCity());
        jobSeekerProfile.setState(addrInfo.getState());

        log.info("JobSeekerProfile 저장: city={}, state={}, lat={}, lng={}",
                addrInfo.getCity(), addrInfo.getState(), coords.get("lat"), coords.get("lng"));

        jobSeekerProfileRepository.save(jobSeekerProfile);

        // 3. 관심사 연결
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

    // 역할 조회
    public IsEmployerResponse isEmployer(String jwt) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);

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
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public JobSeekerProfile getJobSeekerProfileByJwt(String jwt) {
        String firebaseUid = jwtProvider.getFirebaseUid(jwt);
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

    @Getter
    @AllArgsConstructor
    private static class AddressInfo {
        private final String city;
        private final String state;
    }
}
