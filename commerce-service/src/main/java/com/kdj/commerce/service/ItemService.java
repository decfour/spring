package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional  // 쓰기 권한
    public Long saveItem(Item item) {
        Item savedItem = itemRepository.save(item);
        return savedItem.getId();
    }

    public Item findOne(Long itemId) {
        // findById는 Optional을 반환, 데이터가 없으면 null을 반환
        return itemRepository.findById(itemId).orElse(null);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    @Transactional // 영속성 컨텍스트 안에서 트랜잭션이 끝나야 자동으로 Update 쿼리가 나갑니다.
    public void updateItem(Long itemId, Item updateParam) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. id=" + itemId));

        findItem.setName(updateParam.getName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setStock(updateParam.getStock());
        findItem.setDescription(updateParam.getDescription());
        findItem.setOpen(updateParam.isOpen());
        findItem.setItemType(updateParam.getItemType());
        findItem.setDelivery(updateParam.getDelivery()); //
    }
}
