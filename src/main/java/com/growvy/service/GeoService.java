package com.growvy.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j // log를 사용하기 위한 어노테이션
@Service
public class GeoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용

    public Map<String, Double> getCoordinates(String address) {

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .encode() // 여기서 UTF-8 인코딩 수행
                    .toUri(); // String이 아닌 URI 객체 생성

            HttpHeaders headers = new HttpHeaders();

            headers.set("User-Agent", "Growvy/1.0 (s2413@e-mirim.hs.kr)");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("요청 URI: {}", uri);


            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> results = objectMapper.readValue(response.getBody(), List.class);

                if (!results.isEmpty()) {
                    Map<String, Object> first = results.get(0);
                    double lat = Double.parseDouble(first.get("lat").toString());
                    double lng = Double.parseDouble(first.get("lon").toString());

                    log.info("좌표 추출 성공: lat={}, lng={}", lat, lng);
                    return Map.of("lat", lat, "lng", lng);
                } else {
                    log.warn("검색 결과가 비어있습니다. (주소: {})", address);
                }
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP 에러 발생: {} - 본문: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("알 수 없는 에러 발생: {}", e.getMessage(), e);
        }

        return null;
    }
}