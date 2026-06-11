package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.service.NoticeService;
import com.kdj.commerce.web.form.NoticeEditForm;
import com.kdj.commerce.web.form.NoticeSaveForm;
import com.kdj.commerce.web.session.SessionConst;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String list(Model model) {

        List<Notice> notices = noticeService.findNotices();
        model.addAttribute("notices", notices);

        return "notice/noticeList";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Notice notice = noticeService.findOne(id);
        model.addAttribute("notice", notice);

        return "notice/notice";
    }

    @GetMapping("/add")
    public String addForm(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                          Model model) {

        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 작성 폼 진입 시도 차단");
            return "redirect:/notice";
        }

        model.addAttribute("noticeForm", new NoticeSaveForm());

        return "notice/addNoticeForm";
    }

    @PostMapping("/add")
    public String add(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                            @Valid @ModelAttribute("noticeForm") NoticeSaveForm form,
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
        noticeService.saveNotice(notice);

        return "redirect:/notice";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                           @PathVariable Long id,
                           Model model) {

        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 수정 폼 진입 시도 차단");
            return "redirect:/notice";
        }

        Notice notice = noticeService.findOne(id);
        NoticeEditForm form = new NoticeEditForm();
        form.setTitle(notice.getTitle());
        form.setContent(notice.getContent());

        model.addAttribute("noticeForm", form);

        return "notice/editNoticeForm";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                             @Valid @ModelAttribute("noticeForm") NoticeEditForm form,
                             BindingResult bindingResult) {

        if (isNotAdmin(loginMember)) {
            log.warn("권한 없는 사용자의 공지사항 수정 시도 차단");
            return "redirect:/notice";
        }

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "notice/editNoticeForm";
        }

        noticeService.updateNotice(id, form.getTitle(), form.getContent());

        return "redirect:/notice/" + id;
    }

    /*
    @PostMapping("/{id}/delete")
    public String deleteNotice(@PathVariable Long id,
                               @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {

    }
    */
}
