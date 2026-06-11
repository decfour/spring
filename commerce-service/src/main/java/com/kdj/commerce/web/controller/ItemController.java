package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.web.file.FileStore;
import com.kdj.commerce.web.form.item.ItemEditForm;
import com.kdj.commerce.web.form.item.ItemSaveForm;
import com.kdj.commerce.web.session.SessionConst;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

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

    // 상점
    @GetMapping
    public String list(Model model) {
        List<Item> items = itemService.findItemsByDeletedFalse();
        model.addAttribute("items", items);

        return "shop/shop";
    }

    // 상품 추가
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new ItemSaveForm());

        return "shop/addItemForm";
    }

    @PostMapping("/add")
    public String add(@SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
                      @Valid @ModelAttribute ItemSaveForm form,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
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

    // 상품 정보
    @GetMapping("/item/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Item item = itemService.findOne(id);
        model.addAttribute("item", item);

        return "shop/item";
    }

    // 상품 수정
    @GetMapping("/item/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
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
                       @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
                       @Valid @ModelAttribute ItemEditForm form,
                       BindingResult bindingResult) throws IOException {

        if (bindingResult.hasErrors()) {
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
            // 새 사진을 안 올렸으면 기존 이미지를 고스란히 유지
            updateParam.setStoreFileName(findItem.getStoreFileName());
            updateParam.setUploadFileName(findItem.getUploadFileName());
        }

        itemService.updateItem(id, updateParam);

        return "redirect:/shop/item/{id}";
    }

    @PostMapping("/item/{id}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember) {

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
                          @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember) {

        Item item = itemService.findOne(id);
        if (item == null) return "redirect:/shop";

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
