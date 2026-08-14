package com.kdj.commerce.web.dto.walk;

import com.kdj.commerce.domain.walk.WalkTag;
import lombok.Getter;

@Getter
public class WalkCourseTagForm {
    private final String code;
    private final String description;

    public WalkCourseTagForm(WalkTag tag) {
        this.code = tag.name();
        this.description = tag.getDescription();
    }
}
