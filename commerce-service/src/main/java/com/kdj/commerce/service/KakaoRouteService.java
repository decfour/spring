package com.kdj.commerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdj.commerce.web.dto.walk.WalkRouteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KakaoRouteService {

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    private final ObjectMapper objectMapper;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .build();

    public WalkRouteResult findWalkRoute(
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng
    ) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/routing/walk")
                        .queryParam("start_x", startLng)
                        .queryParam("start_y", startLat)
                        .queryParam("end_x", endLng)
                        .queryParam("end_y", endLat)
                        .build())
                .header("Authorization", "KakaoAK " + restApiKey)
                .retrieve()
                .body(String.class);

        return parseRoute(response);
    }

    private WalkRouteResult parseRoute(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode leg = root
                    .path("route")
                    .path("legs")
                    .get(0);

            int distance = leg
                    .path("properties")
                    .path("distance")
                    .asInt();

            int duration = leg
                    .path("properties")
                    .path("time")
                    .asInt();

            List<List<Double>> points = new ArrayList<>();

            JsonNode steps = leg.path("steps");

            for (JsonNode step : steps) {

                JsonNode stepPoints = step
                        .path("path")
                        .path("points");

                for (JsonNode point : stepPoints) {

                    List<Double> coordinate = new ArrayList<>();

                    coordinate.add(point.get(0).asDouble());
                    coordinate.add(point.get(1).asDouble());

                    points.add(coordinate);
                }
            }

            String routeData = objectMapper.writeValueAsString(points);

            return new WalkRouteResult(
                    routeData,
                    distance,
                    duration
            );

        } catch (Exception e) {
            throw new IllegalStateException("카카오 도보 경로 응답 처리 실패", e);
        }
    }
}