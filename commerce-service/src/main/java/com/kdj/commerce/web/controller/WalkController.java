package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkTag;
import com.kdj.commerce.service.WalkCourseService;
import com.kdj.commerce.service.WalkCourseTagService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.walk.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/walk")
public class WalkController {
    private final WalkCourseService walkCourseService;
    private final WalkCourseTagService walkCourseTagService;

    @GetMapping
    public String map() {
        return "walk/main";
    }

    @GetMapping("/course/{id}")
    public String detail(@Login Member loginMember,
                         @PathVariable Long id,
                         Model model) {
        WalkCourse walkCourse = walkCourseService.findOne(id);

        List<WalkCourseTagForm> tags = walkCourseTagService.findTagsByCourseId(id).stream()
                .map(courseTag -> new WalkCourseTagForm(courseTag.getTag()))
                .toList();

        model.addAttribute("loginMember", loginMember);
        model.addAttribute("course", walkCourse);
        model.addAttribute("tags", tags);

        return "walk/detail";
    }

    @PostMapping("/course/{id}/tag")
    @ResponseBody
    public ResponseEntity<?> addTag(@PathVariable Long id,
                                    @RequestBody Map<String, String> request) {
        try {
            String tagCode = request.get("tag");
            WalkTag tag = WalkTag.valueOf(tagCode);

            walkCourseTagService.addTagToCourse(id, tag);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @PostMapping("/course/{id}/delete")
    public String delete(@Login Member loginMember,
                         @PathVariable Long id) {
        WalkCourse walkCourse = walkCourseService.findOne(id);
        if (!isOwner(walkCourse, loginMember) && !isAdmin(loginMember)) {
            return "redirect:/walk/course/" + id;
        }

        walkCourseService.delete(id);

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
            @PageableDefault(size = 3) Pageable pageable,
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

    private boolean isOwner(WalkCourse walkCourse, Member loginMember) {
        if (walkCourse == null || walkCourse.getMember() == null || loginMember == null) {
            return false;
        }
        return walkCourse.getMember().getId().equals(loginMember.getId());
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null && loginMember.getMemberType() == MemberType.ADMIN;
    }
}
