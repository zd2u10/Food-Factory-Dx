package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialArrivalLine;
import com.foodfactory.dx.domain.MaterialOrder;
import com.foodfactory.dx.service.MaterialOrderService;
import com.foodfactory.dx.service.ProcurementService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/material-orders")
public class MaterialOrderController {

    private final MaterialOrderService materialOrderService;
    private final ProcurementService procurementService;

    public MaterialOrderController(MaterialOrderService materialOrderService,
                                    ProcurementService procurementService) {
        this.materialOrderService = materialOrderService;
        this.procurementService = procurementService;
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

    /** 指定した発注に紐づく入荷明細を全件取得する(発注の充足内訳を確認する用途)。 */
    @GetMapping("/{orderId}/lines")
    public List<MaterialArrivalLine> listLines(@PathVariable Long orderId) {
        return procurementService.listByOrderId(orderId);
    }
}
