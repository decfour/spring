package com.kdj.commerce.web.dto.walk;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WalkRouteRequest {
    private Double startLat;
    private Double startLng;

    private Double endLat;
    private Double endLng;
}
