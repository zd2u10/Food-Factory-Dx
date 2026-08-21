package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.CustomerOrder;
import com.foodfactory.dx.domain.OrderLine;
import com.foodfactory.dx.service.CustomerOrderService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-orders")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    public CustomerOrderController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @GetMapping
    public List<CustomerOrder> list() {
        return customerOrderService.listOrders();
    }

    @PostMapping
    public ResponseEntity<CustomerOrder> create(@RequestBody CustomerOrder order) {
        CustomerOrder created = customerOrderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long orderId) {
        customerOrderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long orderId) {
        customerOrderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/lines")
    public List<OrderLine> listLines(@PathVariable Long orderId) {
        return customerOrderService.listOrderLines(orderId);
    }

    @PostMapping("/{orderId}/lines")
    public ResponseEntity<OrderLine> createLine(@PathVariable Long orderId, @RequestBody OrderLine line) {
        line.setOrderId(orderId);
        OrderLine created = customerOrderService.createOrderLine(line);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 受注明細を編集する(商品・数量とも変更可能)。
     * 出荷前の調整に限定しており、既に出荷済みの数量を下回る変更は400エラーになる
     * (GlobalExceptionHandlerがIllegalArgumentExceptionを400として処理する)。
     */
    @PutMapping("/{orderId}/lines/{lineId}")
    public ResponseEntity<OrderLine> updateLine(
            @PathVariable Long orderId,
            @PathVariable Long lineId,
            @RequestBody OrderLine line) {
        line.setOrderId(orderId);
        OrderLine updated = customerOrderService.updateOrderLine(lineId, line);
        return ResponseEntity.ok(updated);
    }
}
