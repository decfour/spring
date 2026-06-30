package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.file.FileStore;
import com.kdj.commerce.web.form.item.ItemEditForm;
import com.kdj.commerce.web.form.item.ItemSaveForm;
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
    private final FileStore fileStore;

    // 공통 모델 바인딩
    @ModelAttribute("itemTypes") public ItemType[] itemTypes() {return ItemType.values();}
    @ModelAttribute("deliveryTypes") public DeliveryType[] deliveryTypes() {return DeliveryType.values();}

    // 검증 메서드
    private boolean isNotOwner(Item item, Member loginMember) {
        return !item.getCreatedBy().equals(loginMember.getId());
    }

    @GetMapping
    public String list(@PageableDefault(size = 9, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Item> items = itemService.findItemsByDeletedFalse(pageable);
        model.addAttribute("items", items);

        return "shop/shop";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new ItemSaveForm());

        return "shop/addItemForm";
    }

    @PostMapping("/add")
    public String add(@Login Member loginMember,
                      @Valid @ModelAttribute("item") ItemSaveForm form,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes,
                      Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", form);
            return "shop/addItemForm";
        }

        String storeFileName = fileStore.storeFile(form.getImageFile());
        String uploadFileName = form.getImageFile() != null ? form.getImageFile().getOriginalFilename() : null;

        Item item = new Item();
        item.setName(form.getName());
        item.setPrice(form.getPrice());
        item.setStock(form.getStock());
        item.setDescription(form.getDescription());
        item.setOpen(form.isOpen());
        item.setItemType(form.getItemType());
        item.setDeliveryType(form.getDeliveryType());
        item.setCreatedBy(loginMember.getId());
        item.setStoreFileName(storeFileName);
        item.setUploadFileName(uploadFileName);

        Long savedItemId = itemService.saveItem(item);
        redirectAttributes.addAttribute("itemId", savedItemId);

        return "redirect:/shop/item/{itemId}";
    }

    @GetMapping("/item/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Item item = itemService.findOne(id);
        model.addAttribute("item", item);

        return "shop/item";
    }

    @GetMapping("/item/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @Login Member loginMember,
                           Model model) {
        Item item = itemService.findOne(id);

        if (isNotOwner(item, loginMember)) {
            log.warn("수정 시도 차단 ID={}, 상품={}", loginMember.getId(), id);
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
        form.setDeleted(item.isDeleted());

        model.addAttribute("item", form);

        return "shop/editItemForm";
    }

    @PostMapping("/item/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Login Member loginMember,
                       @Valid @ModelAttribute("item") ItemEditForm form,
                       BindingResult bindingResult,
                       Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", form);
            return "shop/editItemForm";
        }

        Item findItem = itemService.findOne(id);
        if (isNotOwner(findItem, loginMember)) {
            log.warn("수정 시도 차단 ID={}", loginMember.getId());
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

        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            String storeFileName = fileStore.storeFile(form.getImageFile());
            updateParam.setStoreFileName(storeFileName);
            updateParam.setUploadFileName(form.getImageFile().getOriginalFilename());
        } else {
            updateParam.setStoreFileName(findItem.getStoreFileName());
            updateParam.setUploadFileName(findItem.getUploadFileName());
        }

        itemService.updateItem(id, updateParam);

        return "redirect:/shop/item/{id}";
    }

    @PostMapping("/item/{id}/delete")
    public String delete(@PathVariable Long id,
                         @Login Member loginMember) {
        Item item = itemService.findOne(id);
        if (item == null)
            return "redirect:/shop";

        if (isNotOwner(item, loginMember)) {
            log.warn("상품 삭제 차단={}", id);
            return "redirect:/shop/item/" + id;
        }

        itemService.deleteItem(id);
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

        if (isNotOwner(item, loginMember)) {
            log.warn("상품 복원 차단={}", id);
            return "redirect:/shop/item/" + id;
        }

        itemService.restoreItem(id);
        log.info("상품 복원 완료={}", id);

        return "redirect:/shop/item/" + id;
    }

    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) {
        return new FileSystemResource(fileStore.getFullPath(filename));
    }
}
