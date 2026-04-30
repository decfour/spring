package com.kdj.commerce.repository;

import com.kdj.commerce.domain.Item;

import java.util.List;

public interface ItemRepository {
    public Item save(Item item);
    public Item findById(Long id);
    public List<Item> findAll();
    public void update(Long itemId, Item updateParam);
    public void clearRepository();
}
