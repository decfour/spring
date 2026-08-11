package com.kdj.commerce.web.dto.walk;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WalkRouteResponse {
    private List<RoutePoint> points;
    private Integer distance;
    private Integer duration;
}
