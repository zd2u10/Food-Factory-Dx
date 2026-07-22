package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialOrder;
import com.foodfactory.dx.service.MaterialOrderService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/material-orders")
public class MaterialOrderController {

    private final MaterialOrderService materialOrderService;

    public MaterialOrderController(MaterialOrderService materialOrderService) {
        this.materialOrderService = materialOrderService;
    }

    @GetMapping
    public List<MaterialOrder> list() {
        return materialOrderService.listOrders();
    }

    @PostMapping
    public ResponseEntity<MaterialOrder> create(@RequestBody MaterialOrder order) {
        MaterialOrder created = materialOrderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
