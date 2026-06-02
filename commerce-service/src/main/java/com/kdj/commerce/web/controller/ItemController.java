package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.web.form.ItemEditForm;
import com.kdj.commerce.web.form.ItemSaveForm;
import com.kdj.commerce.web.session.SessionConst;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@RequestMapping("/shop")
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 모든 반환(모델)에 타입 담기
    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {

        return ItemType.values();
    }

    @ModelAttribute("deliveryTypes")
    public DeliveryType[] deliveryTypes() {

        return DeliveryType.values();
    }

    // 상점
    @GetMapping
    public String shop(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);

        return "shop/shop";
    }

    // 아이템 추가
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("item", new ItemSaveForm());

        return "shop/addItemForm";
    }

    @PostMapping("/add")
    public String addItem(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                          @Valid @ModelAttribute ItemSaveForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "shop/addItemForm";
        }

        Item item = new Item();
        item.setName(form.getName());
        item.setPrice(form.getPrice());
        item.setStock(form.getStock());
        item.setDescription(form.getDescription());
        item.setOpen(form.isOpen());
        item.setItemType(form.getItemType());
        item.setDeliveryType(form.getDeliveryType());
        item.setCreatedBy(loginMember.getId());

        Long savedItemId = itemService.saveItem(item);
        redirectAttributes.addAttribute("itemId", savedItemId);

        return "redirect:/shop/item/{itemId}";
    }

    // 아이템 정보
    @GetMapping("/item/{id}")
    public String item(@PathVariable Long id, Model model) {
        Item item = itemService.findOne(id);
        model.addAttribute("item", item);

        return "shop/item";
    }

    // 아이템 수정
    @GetMapping("/item/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                           Model model) {

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        Item item = itemService.findOne(id);

        if (!item.getCreatedBy().equals(loginMember.getId())) {
            log.warn("권한 없는 사용자의 수정 시도 - 유저 ID: {}, 상품 ID: {}", loginMember.getId(), id);
            return "redirect:/shop/item/" + id;
        }

        ItemEditForm form = new ItemEditForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStock(item.getStock());
        form.setDescription(item.getDescription());
        form.setOpen(item.isOpen());
        form.setItemType(item.getItemType());
        form.setDeliveryType(item.getDeliveryType());

        model.addAttribute("item", form);

        return "shop/editItemForm";
    }

    @PostMapping("/item/{id}/edit")
    public String edit(@PathVariable Long id,
                       @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                       @Valid @ModelAttribute ItemEditForm form,
                       BindingResult bindingResult) {

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "shop/editItemForm";
        }

        Item findItem = itemService.findOne(id);
        if (!findItem.getCreatedBy().equals(loginMember.getId())) {
            log.warn("권한 없는 사용자의 상품 수정 시도 거부 - 유저 ID: {}, 상품 ID: {}", loginMember.getId(), id);
            return "redirect:/shop/item/" + id;
        }

        Item updateParam = new Item();
        updateParam.setName(form.getName());
        updateParam.setPrice(form.getPrice());
        updateParam.setStock(form.getStock());
        updateParam.setDescription(form.getDescription());
        updateParam.setOpen(form.isOpen());
        updateParam.setItemType(form.getItemType());
        updateParam.setDeliveryType(form.getDeliveryType());

        updateParam.setCreatedBy(findItem.getCreatedBy());

        itemService.updateItem(id, updateParam);

        return "redirect:/shop/item/{id}";
    }

}
