package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialArrivalLine;
import com.foodfactory.dx.service.ProcurementService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入荷明細(検品結果込み)を登録・取得するためのController。
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

    /** 指定した入荷ヘッダーに属する明細を全件取得する。 */
    @GetMapping
    public List<MaterialArrivalLine> list(@PathVariable Long arrivalId) {
        return procurementService.listByArrivalId(arrivalId);
    }

    /**
     * @PathVariable Long arrivalId: URLの {arrivalId} の部分の値を受け取る。
     *   例えば /api/material-arrivals/3/lines にアクセスした場合、arrivalId には 3 が入る。
     *
     * リクエストボディ(JSON)側には arrivalId を含めなくてよいように、
     * URLから受け取った arrivalId を line にセットしてからServiceに渡している。
     * (同じ情報をURLとJSONボディの両方に書かせると、
     *  食い違った場合にどちらが正しいのか分からなくなるため、URL側を正とする)
     *
     * materialId(必須)・orderId(任意)は、URLではなくリクエストボディ側で指定する。
     * (1回の配送内に複数の材料・複数の発注が混在し得るため、明細ごとに指定する必要がある)
     *
     * resolvesHoldId(任意、クエリパラメータ): この明細が既存の保留に対する交換品である場合に、
     *   その保留のIDを指定する。
     *   例: POST /api/material-arrivals/3/lines?resolvesHoldId=1
     *   指定が無ければ通常の新規入荷として登録される。
     */
    @PostMapping
    public ResponseEntity<MaterialArrivalLine> registerLine(
            @PathVariable Long arrivalId,
            @RequestParam(required = false) Long resolvesHoldId,
            @RequestBody MaterialArrivalLine line) {
        line.setArrivalId(arrivalId);
        MaterialArrivalLine registered = procurementService.registerInspectedLine(line, resolvesHoldId);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }
}
