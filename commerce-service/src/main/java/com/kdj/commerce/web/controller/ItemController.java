package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.item.ItemForm;
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
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        return ItemType.values();
    }

    @ModelAttribute("deliveryTypes")
    public DeliveryType[] deliveryTypes() {
        return DeliveryType.values();
    }

    @GetMapping
    public String list(
            @PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<Item> items = itemService.findActive(pageable);

        model.addAttribute("items", items);

        return "shop/list";
    }

    @GetMapping("/item/{id}")
    public String detail(
            @PathVariable Long id,
            @Login Member loginMember,
            Model model
    ) {
        Item item = itemService.findById(id);

        model.addAttribute("item", item);
        model.addAttribute("member", loginMember);

        return "shop/detail";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new ItemForm());

        return "shop/form";
    }

    @PostMapping("/add")
    public String add(
            @Login Member loginMember,
            @Valid @ModelAttribute("item") ItemForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "shop/form";
        }

        Long itemId = itemService.save(form, loginMember.getId());

        return "redirect:/shop/item/" + itemId;
    }

    @GetMapping("/item/{id}/edit")
    public String editForm(
            @Login Member loginMember,
            @PathVariable Long id,
            Model model
    ) {
        Item item = itemService.findById(id);

        if (!isOwner(item, loginMember) && !isAdmin(loginMember)) {
            log.warn("상품 수정 권한 없음 - memberId={}, itemId={}", loginMember.getId(), id);

            return "redirect:/shop/item/" + id;
        }

        ItemForm form = new ItemForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStock(item.getStock());
        form.setDescription(item.getDescription());
        form.setOpen(item.isOpen());
        form.setDeleted(item.isDeleted());
        form.setItemType(item.getItemType());
        form.setDeliveryType(item.getDeliveryType());

        model.addAttribute("item", form);
        model.addAttribute("isEdit", true);

        return "shop/form";
    }

    @PostMapping("/item/{id}/edit")
    public String edit(
            @Login Member loginMember,
            @PathVariable Long id,
            @Valid @ModelAttribute("item") ItemForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);

            return "shop/form";
        }

        Item item = itemService.findById(id);

        if (!isOwner(item, loginMember) && !isAdmin(loginMember)) {
            log.warn("상품 수정 권한 없음 - memberId={}, itemId={}", loginMember.getId(), id);

            return "redirect:/shop/item/" + id;
        }

        itemService.update(id, form);

        return "redirect:/shop/item/{id}";
    }

    @PostMapping("/item/{id}/delete")
    public String delete(
            @Login Member loginMember,
            @PathVariable Long id
    ) {
        Item item = itemService.findById(id);

        if (!isOwner(item, loginMember) && !isAdmin(loginMember)) {
            log.warn("상품 삭제 권한 없음 - memberId={}, itemId={}", loginMember.getId(), id);

            return "redirect:/shop/item/" + id;
        }

        itemService.delete(id);

        return "redirect:/shop";
    }

    @PostMapping("/item/{id}/restore")
    public String restore(
            @Login Member loginMember,
            @PathVariable Long id
    ) {
        Item item = itemService.findById(id);

        if (!isOwner(item, loginMember)) {
            log.warn("상품 복원 권한 없음 - memberId={}, itemId={}", loginMember.getId(), id);

            return "redirect:/shop/item/" + id;
        }

        itemService.restore(id);

        return "redirect:/shop/item/" + id;
    }

    private boolean isOwner(Item item, Member loginMember) {
        return item.getCreatedBy().equals(loginMember.getId());
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember.getMemberType() == MemberType.ADMIN;
    }
}
