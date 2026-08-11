package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.WalkCourseService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.walk.WalkCourseForm;
import com.kdj.commerce.web.dto.walk.WalkRouteRequest;
import com.kdj.commerce.web.dto.walk.WalkRouteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/walk")
public class WalkController {
    private final WalkCourseService walkCourseService;

    @GetMapping
    public String map() {
        return "walk/main";
    }

    @GetMapping("/add")
    public String addForm() {
        return "walk/form";
    }

    @PostMapping
    public String add(@Login Member loginMember,
                      WalkCourseForm form) {
        System.out.println("name = " + form.getName());
        System.out.println("review = " + form.getReview());
        System.out.println("startLat = " + form.getStartLat());
        System.out.println("startLng = " + form.getStartLng());
        System.out.println("endLat = " + form.getEndLat());
        System.out.println("endLng = " + form.getEndLng());
        System.out.println("routeData = " + form.getRouteData());
        System.out.println("distance = " + form.getDistance());
        System.out.println("duration = " + form.getDuration());

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

    @PostMapping("/route")
    @ResponseBody
    public WalkRouteResult route(@RequestBody WalkRouteRequest request) {

        return walkCourseService.findRoute(
                request.getStartLat(),
                request.getStartLng(),
                request.getEndLat(),
                request.getEndLng()
        );
    }
}
