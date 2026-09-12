package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.ChatRoomService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.chat.ChatCourseResponse;
import com.kdj.commerce.web.dto.chat.ChatRoomDetailResponse;
import com.kdj.commerce.web.dto.chat.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/walk/course/{courseId}/chat")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @GetMapping
    public String list(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "false") boolean mine,
            Model model
    ) {
        if (loginMember == null) {
            return "redirect:/member/sign-in";
        }

        try {
            ChatCourseResponse course = chatRoomService.findCourse(courseId);
            model.addAttribute("course", course);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        Page<ChatRoomResponse> rooms = chatRoomService.findRooms(
                courseId,
                loginMember.getId(),
                mine,
                PageRequest.of(Math.max(0, page), 4)
        );

        model.addAttribute("rooms", rooms);
        model.addAttribute("mine", mine);

        return "chat/home";
    }

    @PostMapping
    public String add(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @RequestParam String title,
            RedirectAttributes redirect
    ) {
        if (loginMember == null) {
            return "redirect:/member/sign-in";
        }

        try {
            Long roomId = chatRoomService.save(courseId, loginMember.getId(), title);

            return "redirect:/walk/course/" + courseId + "/chat/" + roomId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("draftTitle", title);

            return redirectToList(courseId);
        }
    }

    @PostMapping("/{roomId}/join")
    public String join(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @PathVariable Long roomId,
            RedirectAttributes redirect
    ) {
        if (loginMember == null) {
            return "redirect:/member/sign-in";
        }

        try {
            chatRoomService.join(courseId, roomId, loginMember.getId());

            return "redirect:/walk/course/" + courseId + "/chat/" + roomId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());

            return redirectToList(courseId);
        }
    }

    @GetMapping("/{roomId}")
    public String detail(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @PathVariable Long roomId,
            Model model,
            RedirectAttributes redirect
    ) {
        if (loginMember == null) {
            return "redirect:/member/sign-in";
        }

        try {
            ChatRoomDetailResponse detail = chatRoomService.findDetail(
                    courseId,
                    roomId,
                    loginMember.getId()
            );

            model.addAttribute("course", detail.getCourse());
            model.addAttribute("room", detail.getRoom());
            model.addAttribute("participants", detail.getParticipants());
            model.addAttribute("memberId", loginMember.getId());

            return "chat/room";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());

            return redirectToList(courseId);
        }
    }

    @PostMapping("/{roomId}/leave")
    public String leave(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @PathVariable Long roomId,
            RedirectAttributes redirect
    ) {
        if (loginMember == null) {
            return "redirect:/member/sign-in";
        }

        try {
            chatRoomService.leave(courseId, roomId, loginMember.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return redirectToList(courseId);
    }

    private String redirectToList(Long courseId) {
        return "redirect:/walk/course/" + courseId + "/chat";
    }
}
