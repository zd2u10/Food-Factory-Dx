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

    public List<Item> listItems() {
        return itemMapper.findAll();
    }

    public Item createItem(Item item) {
        itemMapper.insert(item);
        return item;
    }
}
