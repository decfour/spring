package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.PostComment;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.PostCommentService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.community.PostCommentForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService postCommentService;

    @PostMapping("/{postId}/add")
    public String add(
            @Login Member loginMember,
            @PathVariable Long postId,
            @Valid @ModelAttribute PostCommentForm form,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "redirect:/community/post/" + postId;
        }

        postCommentService.save(postId, loginMember.getId(), form.getContent());

        return "redirect:/community/post/" + postId;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @Login Member loginMember,
            @PathVariable Long id
    ) {
        PostComment comment = postCommentService.findById(id);

        if (!isOwner(comment, loginMember) && !isAdmin(loginMember)) {
            return "redirect:/community/post/" + comment.getPost().getId();
        }

        postCommentService.delete(id);

        return "redirect:/community/post/" + comment.getPost().getId();
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null && loginMember.getMemberType() == MemberType.ADMIN;
    }

    private boolean isOwner(PostComment comment, Member loginMember) {
        if (comment == null || comment.getCreator() == null || loginMember == null) {
            return false;
        }

        return comment.getCreator().getId().equals(loginMember.getId());
    }
}
