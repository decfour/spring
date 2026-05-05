package com.kdj.commerce.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.repository.ItemRepository;
import com.kdj.commerce.repository.MemoryItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@RequestMapping("/shop")
@Controller
public class ItemController {

    private final ItemRepository itemRepository = new MemoryItemRepository();

    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        return ItemType.values();
    }

    @GetMapping
    public String shop(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);

        return "shop/shop";
    }

    @GetMapping("/item/{id}")
    public String item(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);

        return "shop/item";
    }

    // Model 보내줘야 함
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("item", new Item());

        return "shop/add";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes) {
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());

        return "redirect:/shop/item/{itemId}";
    }

    @GetMapping("/item/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);

        return "shop/edit";
    }

    @PostMapping("/item/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute Item item) {
        itemRepository.update(id, item);

        return "redirect:/shop/item/{id}";
    }

}
