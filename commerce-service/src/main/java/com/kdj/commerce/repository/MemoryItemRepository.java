package com.kdj.commerce.repository;

import com.kdj.commerce.domain.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryItemRepository {

    private static final Map<Long, Item> store = new HashMap<>(); //static
    private static long sequence = 0L;


    public Item save(Item item) {
        item.setId(++sequence);
        store.put(item.getId(), item);
        return item;
    }


    public Item findById(Long id) {
        return store.get(id);
    }


    public List<Item> findAll() {
        return new ArrayList<>(store.values());
    }


    public void update(Long itemId, Item updateParam) {
        Item findItem = findById(itemId);
        findItem.setName(updateParam.getName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }


    public void clearRepository() {
        store.clear();
    }
}
