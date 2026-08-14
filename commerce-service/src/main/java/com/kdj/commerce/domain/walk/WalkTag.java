package com.kdj.commerce.domain.walk;

public enum WalkTag {
    NATURE("자연"),
    CITY("도심"),

    QUIET("조용한"),
    HOT("신나는"),

    NIGHT("저녁"),
    MORNING("아침"),

    LONG_DISTANCE("장거리"),
    SHORT_DISTANCE("단거리"),

    LOVE("데이트"),
    PET("강아지"),
    FAMILY("가족");

    private final String description;

    WalkTag(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
