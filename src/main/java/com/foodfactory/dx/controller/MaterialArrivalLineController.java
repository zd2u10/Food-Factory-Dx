package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialArrivalLine;
import com.foodfactory.dx.service.ProcurementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入荷明細(検品結果込み)を登録するためのController。
 * URLの形が /api/material-arrivals/{arrivalId}/lines となっているのは、
 * 「入荷明細は、必ずどこかの入荷ヘッダーに属する」という親子関係をURL自体で表現するため。
 */
@RestController
@RequestMapping("/api/material-arrivals/{arrivalId}/lines")
public class MaterialArrivalLineController {

    private final ProcurementService procurementService;

    public MaterialArrivalLineController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    /**
     * @PathVariable Long arrivalId: URLの {arrivalId} の部分の値を受け取る。
     *   例えば /api/material-arrivals/3/lines にアクセスした場合、arrivalId には 3 が入る。
     *
     * リクエストボディ(JSON)側には arrivalId を含めなくてよいように、
     * URLから受け取った arrivalId を line にセットしてからServiceに渡している。
     * (同じ情報をURLとJSONボディの両方に書かせると、
     *  食い違った場合にどちらが正しいのか分からなくなるため、URL側を正とする)
     */
    @PostMapping
    public ResponseEntity<MaterialArrivalLine> registerLine(
            @PathVariable Long arrivalId,
            @RequestBody MaterialArrivalLine line) {
        line.setArrivalId(arrivalId);
        MaterialArrivalLine registered = procurementService.registerInspectedLine(line);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }
}
