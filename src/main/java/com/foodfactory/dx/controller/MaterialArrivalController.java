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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/material-arrivals")
public class MaterialArrivalController {

    private final MaterialArrivalService materialArrivalService;

    public MaterialArrivalController(MaterialArrivalService materialArrivalService) {
        this.materialArrivalService = materialArrivalService;
    }

    /**
     * 入荷ヘッダーの一覧を取得する。
     *
     * @RequestParam(required = false) Long orderId:
     *   URLの末尾に "?orderId=1" のようなクエリパラメータが付いていたらその値を受け取り、
     *   付いていなければ null のままにする、という意味。
     *   例:
     *     GET /api/material-arrivals            → 全件取得
     *     GET /api/material-arrivals?orderId=1  → orderId=1の発注に紐づく入荷だけ取得
     */
    @GetMapping
    public List<MaterialArrival> list(@RequestParam(required = false) Long orderId) {
        if (orderId != null) {
            return materialArrivalService.listByOrderId(orderId);
        }
        return materialArrivalService.listAll();
    }

    @PostMapping
    public ResponseEntity<MaterialArrival> create(@RequestBody MaterialArrival arrival) {
        MaterialArrival created = materialArrivalService.createArrival(arrival);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
