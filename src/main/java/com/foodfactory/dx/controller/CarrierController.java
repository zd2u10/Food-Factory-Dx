package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.Carrier;
import com.foodfactory.dx.service.CarrierService;
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
@RequestMapping("/api/carriers")
public class CarrierController {

    private final CarrierService carrierService;

    public CarrierController(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    @GetMapping
    public List<Carrier> list() {
        return carrierService.listCarriers();
    }

    @PostMapping
    public ResponseEntity<Carrier> create(@RequestBody Carrier carrier) {
        Carrier created = carrierService.createCarrier(carrier);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{carrierId}")
    public ResponseEntity<Carrier> update(@PathVariable Long carrierId, @RequestBody Carrier carrier) {
        Carrier updated = carrierService.updateCarrier(carrierId, carrier);
        return ResponseEntity.ok(updated);
    }
}
