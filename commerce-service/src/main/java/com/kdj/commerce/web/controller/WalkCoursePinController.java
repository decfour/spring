package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.WalkCoursePinService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.walk.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/walk/course/{courseId}/pins")
public class WalkCoursePinController {
    private final WalkCoursePinService service;

    @GetMapping
    public List<WalkCoursePinResponse> list(@PathVariable Long courseId, @Login Member member) {
        return service.list(courseId, member);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalkCoursePinResponse create(@PathVariable Long courseId, @Login Member member,
                                        @Valid @RequestBody WalkCoursePinRequest request) {
        return service.create(courseId, member, request);
    }

    @DeleteMapping("/{pinId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long courseId, @PathVariable Long pinId, @Login Member member) {
        service.delete(courseId, pinId, member);
    }
}
