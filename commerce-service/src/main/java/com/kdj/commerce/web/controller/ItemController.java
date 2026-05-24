package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.service.ItemService;
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

    // 모든 반환(모델)에 아이템 타입 담기
    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        return ItemType.values();
    }

    // 상점 페이지
    @GetMapping
    public String shop(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);

        return "shop/shop";
    }

    // 아이템 추가
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("item", new Item());

        return "shop/addItemForm";
    }
    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "shop/addItemForm";
        }

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
    @GetMapping("/item/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Item item = itemService.findOne(id);
        model.addAttribute("item", item);

        return "shop/editItemForm";
    }
    @PostMapping("/item/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute Item item, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "shop/editItemForm";
        }

        itemService.updateItem(id, item);

        return "redirect:/shop/item/{id}";
    }

}
