package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.Comment;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.CommentService;
import com.kdj.commerce.service.PostService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.community.CommentForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final PostService postService;

    private boolean isOwner(Comment comment, Member loginMember) {
        if (comment == null || comment.getWriter() == null || loginMember == null) {
            return false;
        }

        return comment.getWriter().getId().equals(loginMember.getId());
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null && loginMember.getMemberType() == MemberType.ADMIN;
    }

    @PostMapping("/{id}/add")
    public String add(@Login Member loginMember,
                      @PathVariable Long id,
                      @Valid @ModelAttribute CommentForm form,
                      BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/community/post/" + id;
        }

        commentService.save(id, loginMember.getId(), form.getContent());

        return "redirect:/community/post/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@Login Member loginMember,
                         @PathVariable Long id) {
        Comment comment = commentService.findOne(id);

        if (!isOwner(comment, loginMember) && !isAdmin(loginMember)) {
            return "redirect:/community/post/" + comment.getPost().getId();
        }

        commentService.delete(id);

        return "redirect:/community/post/" + comment.getPost().getId();
    }
}
