package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.Supplier;
import com.foodfactory.dx.service.SupplierService;
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
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<Supplier> list(@RequestParam(required = false) Boolean active) {
        return supplierService.listSuppliers(active);
    }

    @PostMapping
    public ResponseEntity<Supplier> create(@RequestBody Supplier supplier) {
        Supplier created = supplierService.createSupplier(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<Supplier> update(@PathVariable Long supplierId, @RequestBody Supplier supplier) {
        Supplier updated = supplierService.updateSupplier(supplierId, supplier);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{supplierId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long supplierId) {
        supplierService.deactivateSupplier(supplierId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{supplierId}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable Long supplierId) {
        supplierService.reactivateSupplier(supplierId);
        return ResponseEntity.ok().build();
    }
}
