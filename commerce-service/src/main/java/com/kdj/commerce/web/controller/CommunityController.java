package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.CommunityPost;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.CommunityService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.form.community.PostForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model){
        Page<CommunityPost> communityPosts = communityService.findAll(pageable);
        model.addAttribute("communityPosts", communityPosts);

        return "community/list";
    }

    @GetMapping("/hits")
    public String hitList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                          Model model) {
        Page<CommunityPost> communityPosts = communityService.findHit(pageable);
        model.addAttribute("communityPosts", communityPosts);

        return "community/hit";
    }

    @GetMapping("/{id}")
    public String detail (@PathVariable Long id,
                          @Login Member loginMember,
                          Model model) {
        model.addAttribute("post", communityService.increaseViewCount(id));

        return "community/detail";
    }

    @PostMapping("/{id}/like")
    public String like (@PathVariable Long id,
                        @Login Member loginmember) {
        communityService.increaseLikeCount(id);

        return "redirect:/community/" + id;
    }

    @GetMapping("/add")
    public String addForm(@Login Member loginMember,
                          Model model) {
        model.addAttribute("postForm", new PostForm());

        return "community/form";
    }

    @PostMapping("/add")
    public String add(@Login Member loginMember,
                      @Valid @ModelAttribute("postForm") PostForm form,
                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "community/form";
        }

        CommunityPost communityPost = new CommunityPost();
        communityPost.setTitle(form.getTitle());
        communityPost.setContent(form.getContent());
        communityPost.setWriter(loginMember);
        communityPost.setViewCount(0);
        communityPost.setLikeCount(0);

        communityService.save(communityPost);

        return "redirect:/community";
    }
}
