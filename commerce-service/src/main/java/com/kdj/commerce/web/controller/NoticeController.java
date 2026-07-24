package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.service.NoticeService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.form.notice.NoticeForm;
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

@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {
    private final NoticeService noticeService;

    private boolean isNotAdmin(Member loginMember) {
        return loginMember == null || loginMember.getMemberType() != MemberType.ADMIN;
    }

    @GetMapping
    public String list(@Login Member loginMember,
                       @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Notice> notices = noticeService.findAll(pageable);
        model.addAttribute("notices", notices);

        if (loginMember != null) {
            model.addAttribute("member", loginMember);
        }

        return "notice/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @Login Member loginMember,
                         Model model) {
        Notice notice = noticeService.findOne(id);
        model.addAttribute("notice", notice);
        model.addAttribute("member", loginMember);

        return "notice/detail";
    }

    @GetMapping("/add")
    public String addForm(@Login Member loginMember,
                          Model model) {
        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 작성 폼 진입 시도 차단");
            return "redirect:/notice";
        }
        model.addAttribute("noticeForm", new NoticeForm());

        return "notice/addNoticeForm";
    }

    @PostMapping("/add")
    public String add(@Login Member loginMember,
                      @Valid @ModelAttribute("noticeForm") NoticeForm form,
                      BindingResult bindingResult) {
        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 등록 시도 차단");
            return "redirect:/notice";
        }

        if (bindingResult.hasErrors()) {
            return "notice/addNoticeForm";
        }

        Notice notice = new Notice();
        notice.setTitle(form.getTitle());
        notice.setContent(form.getContent());
        noticeService.save(notice);

        return "redirect:/notice";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@Login Member loginMember,
                           @PathVariable Long id,
                           Model model) {
        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 수정 폼 진입 시도 차단");
            return "redirect:/notice";
        }

        Notice notice = noticeService.findOne(id);
        NoticeForm form = new NoticeForm();
        form.setTitle(notice.getTitle());
        form.setContent(notice.getContent());
        model.addAttribute("noticeForm", form);

        return "notice/editNoticeForm";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Login Member loginMember,
                       @Valid @ModelAttribute("noticeForm") NoticeForm form,
                       BindingResult bindingResult) {
        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 수정 시도 차단");
            return "redirect:/notice";
        }

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "notice/editNoticeForm";
        }

        noticeService.update(id, form.getTitle(), form.getContent());

        return "redirect:/notice/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @Login Member loginMember) {
        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 삭제 시도 차단");
            return "redirect:/notice";
        }

        noticeService.delete(id);

        return "redirect:/notice";
    }
}
