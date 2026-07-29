package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.Shipment;
import com.foodfactory.dx.dto.RegisterShipmentLineRequest;
import com.foodfactory.dx.dto.ShipmentAllocationResult;
import com.foodfactory.dx.service.ShipmentService;
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
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/api/shipments")
    public List<Shipment> list() {
        return shipmentService.listShipments();
    }

    @PostMapping("/api/shipments")
    public ResponseEntity<Shipment> create(@RequestBody Shipment shipment) {
        Shipment created = shipmentService.createShipment(shipment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 指定した受注明細について、出荷のFEFO自動選定をプレビューする(在庫は変更しない)。 */
    @GetMapping("/api/order-lines/{orderLineId}/shipment-preview")
    public ShipmentAllocationResult preview(@PathVariable Long orderLineId) {
        return shipmentService.previewShipmentAllocation(orderLineId);
    }

    /** 出荷明細を登録し、対応するバッチの残量を減らす。 */
    @PostMapping("/api/shipments/{shipmentId}/lines")
    public ResponseEntity<Void> registerLines(
            @PathVariable Long shipmentId,
            @RequestBody RegisterShipmentLineRequest request) {
        shipmentService.registerShipmentLines(shipmentId, request.getOrderLineId(), request.getAllocations());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
