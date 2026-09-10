package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.ChatRoomService;
import com.kdj.commerce.service.ChatRoomViewService;
import com.kdj.commerce.web.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/walk/course/{courseId}/chat")
public class ChatRoomController {
    private final ChatRoomService service;
    private final ChatRoomViewService views;

    @GetMapping
    public String home(@PathVariable Long courseId, @Login Member member,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "false") boolean mine, Model model) {
        if (member == null) return "redirect:/member/sign-in";
        try {
            model.addAttribute("course", views.course(courseId));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        model.addAttribute("rooms", views.list(courseId, member.getId(), mine, PageRequest.of(Math.max(0, page), 4)));
        model.addAttribute("mine", mine);
        return "chat/home";
    }

    @PostMapping
    public String create(@PathVariable Long courseId, @Login Member member,
                         @RequestParam String title, RedirectAttributes redirect) {
        if (member == null) return "redirect:/member/sign-in";
        try {
            Long roomId = service.create(courseId, member.getId(), title);
            return homeUrl(courseId) + "/" + roomId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("draftTitle", title);
            return homeUrl(courseId);
        }
    }

    @PostMapping("/{roomId}/join")
    public String join(@PathVariable Long courseId, @PathVariable Long roomId,
                       @Login Member member, RedirectAttributes redirect) {
        if (member == null) return "redirect:/member/sign-in";
        try {
            views.requireCourse(courseId, roomId);
            service.join(roomId, member.getId());
            return homeUrl(courseId) + "/" + roomId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return homeUrl(courseId);
        }
    }

    @GetMapping("/{roomId}")
    public String room(@PathVariable Long courseId, @PathVariable Long roomId,
                       @Login Member member, Model model, RedirectAttributes redirect) {
        if (member == null) return "redirect:/member/sign-in";
        try {
            var detail = views.detail(courseId, roomId, member.getId());
            model.addAttribute("course", detail.course());
            model.addAttribute("room", detail.room());
            model.addAttribute("participants", detail.participants());
            model.addAttribute("memberId", member.getId());
            return "chat/room";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return homeUrl(courseId);
        }
    }

    @PostMapping("/{roomId}/leave")
    public String leave(@PathVariable Long courseId, @PathVariable Long roomId,
                        @Login Member member, RedirectAttributes redirect) {
        if (member == null) return "redirect:/member/sign-in";
        try {
            views.requireCourse(courseId, roomId);
            service.leave(roomId, member.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return homeUrl(courseId);
    }

    private String homeUrl(Long courseId) {
        return "redirect:/walk/course/" + courseId + "/chat";
    }
}
