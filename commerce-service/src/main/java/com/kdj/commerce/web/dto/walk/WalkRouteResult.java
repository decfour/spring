package com.kdj.commerce.web.dto.walk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WalkRouteResult {
    private String routeData;
    private Integer distance;
    private Integer duration;
}
