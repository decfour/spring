package com.kdj.food.common.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class ApiPagination {
    private ApiPagination() {
    }

    public static PageRequest page(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지는 0 이상, 페이지 크기는 1~100이어야 합니다.");
        }
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("페이지 범위를 초과했습니다.");
        }
        return PageRequest.of(page, size);
    }

    public static PageRequest latestFirst(int page, int size) {
        return page(page, size).withSort(Sort.Direction.DESC, "id");
    }
}
