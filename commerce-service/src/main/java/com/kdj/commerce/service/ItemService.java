package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Item findOne(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + itemId));
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public Long saveItem(Item item) {
        Item savedItem = itemRepository.save(item);

        return savedItem.getId();
    }

    @Transactional
    public void updateItem(Long itemId, Item updateParam) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + itemId));

        findItem.setName(updateParam.getName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setStock(updateParam.getStock());
        findItem.setDescription(updateParam.getDescription());
        findItem.setOpen(updateParam.isOpen());
        findItem.setItemType(updateParam.getItemType());
        findItem.setDeliveryType(updateParam.getDeliveryType());
        findItem.setStoreFileName(updateParam.getStoreFileName());
        findItem.setUploadFileName(updateParam.getUploadFileName());
    }

    @Transactional
    public void deleteItem(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + itemId));
        findItem.delete();
    }

    @Transactional
    public void restoreItem(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + itemId));
        findItem.restore();
    }

    public List<Item> findItemsByCreatedBy(Long id) {
        return itemRepository.findByCreatedBy(id);
    }

    public Page<Item> findItemsByDeletedFalse(Pageable pageable) {
        return itemRepository.findByDeletedFalse(pageable);
    }
}
