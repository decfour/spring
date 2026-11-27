package com.kdj.food.common.web;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> items, int page, int size, long totalElements) {
    public static <T> PageResponse<T> from(Page<T> results) {
        return new PageResponse<>(results.getContent(), results.getNumber(),
                results.getSize(), results.getTotalElements());
    }
}
