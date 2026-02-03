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

@Slf4j
@Service
public class GeoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 주소를 받아서 lat/lng, state, city 정보를 반환
     * 반환 타입: Map<String, Object> → lat/lng(Double), state/city(String)
     */
    public Map<String, Object> getCoordinates(String address) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .encode()
                    .toUri();

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

                    // 좌표
                    Double lat = Double.parseDouble(first.get("lat").toString());
                    Double lng = Double.parseDouble(first.get("lon").toString());

                    // state, city 추출
                    Map<String, Object> addressMap = (Map<String, Object>) first.get("address");
                    String state = addressMap.getOrDefault("state", "").toString();
                    String city = addressMap.getOrDefault("city", "").toString();

                    log.info("좌표 및 행정 구역 추출 성공: lat={}, lng={}, state={}, city={}", lat, lng, state, city);

                    return Map.of(
                            "lat", lat,
                            "lng", lng,
                            "state", state,
                            "city", city
                    );
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