package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.Item;
import com.foodfactory.dx.service.ItemService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<Item> list(@RequestParam(required = false) Boolean active) {
        return itemService.listItems(active);
    }

    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        Item created = itemService.createItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<Item> update(@PathVariable Long itemId, @RequestBody Item item) {
        Item updated = itemService.updateItem(itemId, item);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{itemId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long itemId) {
        itemService.deactivateItem(itemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{itemId}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable Long itemId) {
        itemService.reactivateItem(itemId);
        return ResponseEntity.ok().build();
    }
}
