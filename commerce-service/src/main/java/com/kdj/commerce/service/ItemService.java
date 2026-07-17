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

    public Item findOne(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Transactional
    public Long save(Item item) {
        Item savedItem = itemRepository.save(item);

        return savedItem.getId();
    }

    @Transactional
    public void update(Long id, Item updateParam) {
        Item findItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));

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
    public void delete(Long id) {
        Item findItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));
        findItem.delete();
    }

    @Transactional
    public void restore(Long id) {
        Item findItem = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + id));
        findItem.restore();
    }

    public List<Item> findByCreatedBy(Long id) {
        return itemRepository.findByCreatedBy(id);
    }

    public Page<Item> findActive(Pageable pageable) {
        return itemRepository.findByDeletedFalse(pageable);
    }
}
