package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.MaterialOrder;
import com.foodfactory.dx.mapper.MaterialOrderMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MaterialOrderService {

    private final MaterialOrderMapper materialOrderMapper;

    public MaterialOrderService(MaterialOrderMapper materialOrderMapper) {
        this.materialOrderMapper = materialOrderMapper;
    }

    public MaterialOrder createOrder(MaterialOrder order) {
        materialOrderMapper.insert(order);
        return order;
    }

    public List<MaterialOrder> listOrders() {
        return materialOrderMapper.findAll();
    }
}
