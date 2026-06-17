package com.growvy.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private final long expiration = 1000L * 60 * 60 * 24 * 30; // 30일

    // 백엔드 자체 토큰 생성용 (로그인/회원가입 성공 시 프론트엔드에 내려줄 용도)
    public String createToken(String firebaseUid, Long userId) {
        return Jwts.builder()
                .setSubject(firebaseUid) // 여기에 이미 firebaseUid가 들어가 있습니다!
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    // 백엔드가 만든 자체 JWT 전용 파싱 메서드 (Identity 대칭키 방식)
    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 자체 JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 이 메서드는 로그인 API(/api/auth/login)에서 '파이어베이스 ID 토큰'을 검증할 때만 써야 합니다!
    public String getFirebaseUid(String token) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            return decodedToken.getUid();
        } catch (Exception e) {
            throw new RuntimeException("Firebase 토큰 검증 및 UID 추출 실패: " + e.getMessage());
        }
    }

    //  백엔드가 발행한 '자체 JWT'에서 Firebase UID(Subject)를 추출하는 메서드
    public String getFirebaseUidFromBackendToken(String token) {
        return parse(token).getSubject();
    }

    // 자체 JWT에서 userId 추출
    public Long getUserId(String token) {
        Object val = parse(token).get("userId");
        return val == null ? null : Long.valueOf(val.toString());
    }
}