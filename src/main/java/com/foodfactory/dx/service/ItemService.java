package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Item;
import com.foodfactory.dx.mapper.ItemMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemMapper itemMapper;

    public ItemService(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public List<Item> listItems(Boolean active) {
        return itemMapper.findByFilters(active);
    }

    public Item createItem(Item item) {
        itemMapper.insert(item);
        return item;
    }

    public Item updateItem(Long itemId, Item item) {
        itemMapper.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません: itemId=" + itemId));
        item.setItemId(itemId);
        itemMapper.update(item);
        return item;
    }

    public void deactivateItem(Long itemId) {
        itemMapper.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません: itemId=" + itemId));
        itemMapper.setActive(itemId, false);
    }

    public void reactivateItem(Long itemId) {
        itemMapper.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません: itemId=" + itemId));
        itemMapper.setActive(itemId, true);
    }
}
