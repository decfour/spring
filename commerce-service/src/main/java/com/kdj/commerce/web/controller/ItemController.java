package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.file.FileStore;
import com.kdj.commerce.web.dto.item.ItemForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @ModelAttribute("itemTypes") public ItemType[] itemTypes() {return ItemType.values();}
    @ModelAttribute("deliveryTypes") public DeliveryType[] deliveryTypes() {return DeliveryType.values();}

    private boolean isOwner(Item item, Member loginMember) {
        return item.getCreatedBy().equals(loginMember.getId());
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember.getMemberType() == MemberType.ADMIN;
    }

    @GetMapping
    public String list(@PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Item> items = itemService.findActive(pageable);
        model.addAttribute("items", items);

        return "shop/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new ItemForm());

        return "shop/form";
    }

    @PostMapping("/add")
    public String add(@Login Member loginMember,
                      @Valid @ModelAttribute("item") ItemForm form,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", form);
            return "shop/form";
        }

        Long itemId = itemService.save(form, loginMember.getId());

        redirectAttributes.addAttribute("itemId", itemId);

        return "redirect:/shop/item/{itemId}";
    }

    @GetMapping("/item/{id}")
    public String detail(@PathVariable Long id,
                         @Login Member loginMember,
                         Model model) {
        Item item = itemService.findOne(id);
        model.addAttribute("item", item);
        model.addAttribute("member", loginMember);

        return "shop/detail";
    }

    @GetMapping("/item/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @Login Member loginMember,
                           Model model) {
        Item item = itemService.findOne(id);

        if (!isOwner(item, loginMember) && !isAdmin(loginMember)) {
            log.warn("수정 시도 차단 ID={}, 상품={}", loginMember.getId(), id);
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
    public String edit(@PathVariable Long id,
                       @Login Member loginMember,
                       @Valid @ModelAttribute("item") ItemForm form,
                       BindingResult bindingResult,
                       Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", form);
            return "shop/form";
        }

        Item findItem = itemService.findOne(id);
        if (!isOwner(findItem, loginMember) && !isAdmin(loginMember)) {
            log.warn("수정 시도 차단 ID={}", loginMember.getId());
            return "redirect:/shop/item/" + id;
        }

        itemService.update(id, form);

        return "redirect:/shop/item/{id}";
    }

    @PostMapping("/item/{id}/delete")
    public String delete(@PathVariable Long id,
                         @Login Member loginMember) {
        Item item = itemService.findOne(id);
        if (item == null)
            return "redirect:/shop";

        if (!isOwner(item, loginMember) && !isAdmin(loginMember)) {
            log.warn("상품 삭제 차단={}", id);
            return "redirect:/shop/item/" + id;
        }

        itemService.delete(id);
        log.info("상품 삭제 완료={}", id);

        return "redirect:/shop";
    }

    @PostMapping("/item/{id}/restore")
    public String restore(@PathVariable Long id,
                          @Login Member loginMember) {
        Item item = itemService.findOne(id);
        if (item == null) {
            return "redirect:/shop";
        }

        if (!isOwner(item, loginMember)) {
            log.warn("상품 복원 차단={}", id);
            return "redirect:/shop/item/" + id;
        }

        itemService.restore(id);
        log.info("상품 복원 완료={}", id);

        return "redirect:/shop/item/" + id;
    }
}
