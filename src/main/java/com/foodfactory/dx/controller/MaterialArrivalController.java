package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialArrival;
import com.foodfactory.dx.service.MaterialArrivalService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入荷ヘッダー(伝票)のController。
 * ヘッダーは配送イベントの情報(いつ・どの仕入先か)だけを持つため、
 * 発注(orderId)による絞り込みはヘッダー側では行わない
 * (発注に紐づく明細を確認したい場合は MaterialArrivalLineController 側のAPIを使う)。
 */
@RestController
@RequestMapping("/api/material-arrivals")
public class MaterialArrivalController {

    private final MaterialArrivalService materialArrivalService;

    public MaterialArrivalController(MaterialArrivalService materialArrivalService) {
        this.materialArrivalService = materialArrivalService;
    }

    @GetMapping
    public List<MaterialArrival> list() {
        return materialArrivalService.listAll();
    }

    @PostMapping
    public ResponseEntity<MaterialArrival> create(@RequestBody MaterialArrival arrival) {
        MaterialArrival created = materialArrivalService.createArrival(arrival);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
