package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.service.WalkCourseService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.walk.WalkCourseForm;
import com.kdj.commerce.web.dto.walk.WalkCourseResponse;
import com.kdj.commerce.web.dto.walk.WalkRouteRequest;
import com.kdj.commerce.web.dto.walk.WalkRouteResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/walk")
public class WalkController {
    private final WalkCourseService walkCourseService;

    @GetMapping
    public String map() {
        return "walk/main";
    }

    @GetMapping("/course/{id}")
    public String detail(@PathVariable Long id, Model model) {
        WalkCourse walkCourse = walkCourseService.findOne(id);
        model.addAttribute("course", walkCourse);

        return "walk/detail";
    }

    @GetMapping("/course/add")
    public String addForm() {
        return "walk/form";
    }

    @PostMapping
    public String add(@Login Member loginMember,
                      @Valid @ModelAttribute WalkCourseForm form,
                      BindingResult result) {
        if (result.hasErrors()) {
            return "walk/form";
        }

        Long courseId = walkCourseService.save(
                loginMember,
                form.getName(),
                form.getReview(),
                form.getStartLat(),
                form.getStartLng(),
                form.getEndLat(),
                form.getEndLng(),
                form.getRouteData(),
                form.getDistance(),
                form.getDuration()
        );

        return "redirect:/walk";
    }

    @PostMapping("/course/route")
    @ResponseBody
    public WalkRouteResult route(@RequestBody WalkRouteRequest request) {
        return walkCourseService.findRoute(
                request.getStartLat(),
                request.getStartLng(),
                request.getEndLat(),
                request.getEndLng()
        );
    }

    @GetMapping("/course/nearby")
    @ResponseBody
    public Page<WalkCourseResponse> nearbyCourses(
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        return walkCourseService.findNearbyCourses(pageable, lat, lng);
    }

    @PostMapping("/course/{id}/like")
    @ResponseBody
    public int like(@PathVariable Long id,
                       @Login Member loginMember) {
        return walkCourseService.like(id, loginMember);
    }
}
