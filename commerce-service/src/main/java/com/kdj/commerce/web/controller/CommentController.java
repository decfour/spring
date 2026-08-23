package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.Comment;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.CommentService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.community.CommentForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{postId}/add")
    public String add(
            @Login Member loginMember,
            @PathVariable Long postId,
            @Valid @ModelAttribute CommentForm form,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "redirect:/community/post/" + postId;
        }

        commentService.save(postId, loginMember.getId(), form.getContent());

        return "redirect:/community/post/" + postId;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @Login Member loginMember,
            @PathVariable Long id
    ) {
        Comment comment = commentService.findById(id);

        if (!isOwner(comment, loginMember) && !isAdmin(loginMember)) {
            return "redirect:/community/post/" + comment.getPost().getId();
        }

        commentService.delete(id);

        return "redirect:/community/post/" + comment.getPost().getId();
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null && loginMember.getMemberType() == MemberType.ADMIN;
    }

    private boolean isOwner(Comment comment, Member loginMember) {
        if (comment == null || comment.getWriter() == null || loginMember == null) {
            return false;
        }

        return comment.getWriter().getId().equals(loginMember.getId());
    }
}
