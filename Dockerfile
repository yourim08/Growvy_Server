# 1단계: 빌드
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Gradle Wrapper, Gradle 설정 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* ./

# 소스 코드 복사
COPY src src

# 실행 권한 부여 후 빌드
RUN chmod +x gradlew && ./gradlew clean bootJar -x test

# 2단계: 실행용 경량 이미지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]
