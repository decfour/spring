package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.service.NoticeService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.notice.NoticeForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    @GetMapping
    public String list(
            @Login Member loginMember,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<Notice> notices = noticeService.findAll(pageable);

        model.addAttribute("member", loginMember);
        model.addAttribute("notices", notices);

        return "notice/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @Login Member loginMember,
            @PathVariable Long id,
            Model model
    ) {
        Notice notice = noticeService.findById(id);

        model.addAttribute("notice", notice);
        model.addAttribute("member", loginMember);

        return "notice/detail";
    }

    @GetMapping("/add")
    public String addForm(
            @Login Member loginMember,
            Model model
    ) {
        if (!isAdmin(loginMember)) {
            log.warn("공지사항 작성 권한 없음");

            return "redirect:/notice";
        }

        model.addAttribute("noticeForm", new NoticeForm());
        model.addAttribute("isEdit", false);

        return "notice/form";
    }

    @PostMapping("/add")
    public String add(
            @Login Member loginMember,
            @Valid @ModelAttribute("noticeForm") NoticeForm form,
            BindingResult bindingResult
    ) {
        if (!isAdmin(loginMember)) {
            log.warn("공지사항 작성 권한 없음");

            return "redirect:/notice";
        }

        if (bindingResult.hasErrors()) {
            return "notice/form";
        }

        Long noticeId = noticeService.save(form.getTitle(), form.getContent());

        return "redirect:/notice/" + noticeId;
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @Login Member loginMember,
            @PathVariable Long id,
            Model model
    ) {
        if (!isAdmin(loginMember)) {
            log.warn("공지사항 수정 권한 없음");

            return "redirect:/notice";
        }

        Notice notice = noticeService.findById(id);

        NoticeForm form = new NoticeForm();
        form.setId(notice.getId());
        form.setTitle(notice.getTitle());
        form.setContent(notice.getContent());

        model.addAttribute("noticeForm", form);
        model.addAttribute("isEdit", true);

        return "notice/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @Login Member loginMember,
            @PathVariable Long id,
            @Valid @ModelAttribute("noticeForm") NoticeForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (!isAdmin(loginMember)) {
            log.warn("공지사항 수정 권한 없음");

            return "redirect:/notice";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);

            return "notice/form";
        }

        noticeService.update(id, form.getTitle(), form.getContent());

        return "redirect:/notice/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @Login Member loginMember,
            @PathVariable Long id
    ) {
        if (!isAdmin(loginMember)) {
            log.warn("공지사항 삭제 권한 없음");

            return "redirect:/notice";
        }

        noticeService.delete(id);

        return "redirect:/notice";
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null
                && loginMember.getMemberType() == MemberType.ADMIN;
    }
}
