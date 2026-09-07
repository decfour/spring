package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.PostComment;
import com.kdj.commerce.domain.community.Post;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.PostCommentService;
import com.kdj.commerce.service.PostService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.community.PostForm;
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
@RequestMapping("/community")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final PostCommentService postCommentService;

    @GetMapping
    public String list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<Post> posts = postService.findAll(pageable);

        model.addAttribute("posts", posts);

        return "community/list";
    }

    @GetMapping("/hits")
    public String hitList(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<Post> posts = postService.findHit(pageable);

        model.addAttribute("posts", posts);

        return "community/hit";
    }

    @GetMapping("/post/{id}")
    public String detail(
            @Login Member loginMember,
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        postService.increaseViewCount(id);

        Post post = postService.findById(id);
        Page<PostComment> comments = postCommentService.findByPostId(id, pageable);

        model.addAttribute("post", post);
        model.addAttribute("loginMember", loginMember);
        model.addAttribute("comments", comments);

        return "community/detail";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("postForm", new PostForm());

        return "community/form";
    }

    @PostMapping("/add")
    public String add(
            @Login Member loginMember,
            @Valid @ModelAttribute("postForm") PostForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "community/form";
        }

        postService.save(form.getTitle(), form.getContent(), loginMember);

        return "redirect:/community";
    }

    @GetMapping("/post/{id}/edit")
    public String editForm(
            @Login Member loginMember,
            @PathVariable Long id,
            Model model
    ) {
        Post post = postService.findById(id);

        if (!isOwner(post, loginMember) && !isAdmin(loginMember)) {
            log.warn("수정 시도 차단 ID={}, 게시글={}", loginMember != null ? loginMember.getId() : "비로그인", id);
            return "redirect:/community/post/" + id;
        }

        PostForm form = new PostForm();
        form.setId(post.getId());
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());

        model.addAttribute("postForm", form);
        model.addAttribute("isEdit", true);

        return "community/form";
    }

    @PostMapping("/post/{id}/edit")
    public String edit(
            @Login Member loginMember,
            @PathVariable Long id,
            @Valid @ModelAttribute("postForm") PostForm form,
            BindingResult bindingResult,
            Model model
    ) {
        Post findPost = postService.findById(id);

        if (!isOwner(findPost, loginMember) && !isAdmin(loginMember)) {
            log.warn("수정 시도 차단 ID={}, 게시글={}", loginMember != null ? loginMember.getId() : "비로그인", id);
            return "redirect:/community/post/" + id;
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "community/form";
        }

        postService.update(id, form.getTitle(), form.getContent());

        return "redirect:/community/post/" + id;
    }

    @PostMapping("/post/{id}/delete")
    public String delete(
            @Login Member loginMember,
            @PathVariable Long id
    ) {
        Post findPost = postService.findById(id);
        if (!isOwner(findPost, loginMember) && !isAdmin(loginMember)) {
            log.warn("삭제 시도 차단 ID={}, 게시글={}", loginMember != null ? loginMember.getId() : "비로그인", id);
            return "redirect:/community/post/" + id;
        }
        postService.delete(id);

        return "redirect:/community";
    }

    @PostMapping("/post/{id}/like")
    public String like(
            @Login Member loginMember,
            @PathVariable Long id
    ) {
        postService.increaseLikeCount(id, loginMember);

        return "redirect:/community/post/" + id;
    }

    private boolean isOwner(Post post, Member loginMember) {
        if (post == null || post.getCreator() == null || loginMember == null) {
            return false;
        }
        return post.getCreator().getId().equals(loginMember.getId());
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null && loginMember.getMemberType() == MemberType.ADMIN;
    }
}
