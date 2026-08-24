package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialLot;
import com.foodfactory.dx.dto.DiscardUsageRequest;
import com.foodfactory.dx.dto.FefoAllocationLine;
import com.foodfactory.dx.dto.MarkNeedsReviewRequest;
import com.foodfactory.dx.dto.ResolveReviewRequest;
import com.foodfactory.dx.dto.SwitchLotRequest;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import com.foodfactory.dx.service.ManufacturingService;
import com.foodfactory.dx.service.MaterialLotService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 材料ロットは「入荷検品を登録した結果として自動生成される」ものであり、
 * 人が直接ロットを新規作成するAPIは用意していない
 * (登録経路をProcurementService経由の1つに絞ることで、
 *  在庫データの発生源を追いやすくするため)。
 *
 * ただし、製造実行画面での「別ロットに切り替える」操作、および
 * 在庫画面での「検査結果を登録する」操作は、既存ロットの状態を変更する
 * 業務ロジックを伴うため、それぞれ ManufacturingService / MaterialLotService
 * を経由する(要件定義書8.21節を参照)。
 */
@RestController
@RequestMapping("/api/material-lots")
public class MaterialLotController {

    private final MaterialLotMapper materialLotMapper;
    private final ManufacturingService manufacturingService;
    private final MaterialLotService materialLotService;

    public MaterialLotController(MaterialLotMapper materialLotMapper, ManufacturingService manufacturingService,
                                  MaterialLotService materialLotService) {
        this.materialLotMapper = materialLotMapper;
        this.manufacturingService = manufacturingService;
        this.materialLotService = materialLotService;
    }

    /** 指定した材料のロットを、賞味期限が近い順(FEFO順)に取得する。 */
    @GetMapping
    public List<MaterialLot> listByMaterial(@RequestParam(required = false) Long materialId) {
        if (materialId != null) {
            return materialLotMapper.findByMaterialIdOrderByExpiry(materialId);
        }
        // materialId未指定の場合は、在庫画面向けに残量が残っている全ロットを返す。
        return materialLotMapper.findAllWithRemainingQty();
    }

    /** 要確認フラグが立っている、全ロットを取得する(在庫画面の要確認セクション用)。 */
    @GetMapping("/needs-review")
    public List<MaterialLot> listNeedsReview() {
        return materialLotService.listNeedsReview();
    }

    /**
     * 製造実行画面で「別ロットに切り替える」操作を行う。
     * 対象のロットに要確認フラグを立て、同じ材料について改めてFEFO選定を行った結果を返す。
     */
    @PostMapping("/{lotId}/switch")
    public List<FefoAllocationLine> switchLot(@PathVariable Long lotId, @RequestBody SwitchLotRequest request) {
        return manufacturingService.switchLot(
                lotId, request.getReviewReason(), request.getReviewComment(), request.getItemId());
    }

    /**
     * 在庫画面(棚卸・日常の在庫管理)から、ロットを要確認状態にする。
     * 製造実行画面のswitchLotとは異なり、再選定は行わず、要確認にするだけの単独操作
     * (在庫管理の担当者が、製造中の特定バッチとは無関係に、単独で判断するケースに対応)。
     */
    @PostMapping("/{lotId}/mark-needs-review")
    public ResponseEntity<Void> markNeedsReview(@PathVariable Long lotId, @RequestBody MarkNeedsReviewRequest request) {
        manufacturingService.markLotAsNeedsReview(lotId, request.getReviewReason(), request.getReviewComment());
        return ResponseEntity.ok().build();
    }

    /** 検査結果を登録する(生存量方式。0を指定すれば全量破棄と同じ結果になる)。 */
    @PostMapping("/{lotId}/resolve-review")
    public ResponseEntity<Void> resolveReview(@PathVariable Long lotId, @RequestBody ResolveReviewRequest request) {
        materialLotService.resolveReview(lotId, request.getSurvivingQty(), request.getReason(), request.getComment());
        return ResponseEntity.ok().build();
    }

    /**
     * 製造実行画面の「破棄する」操作。実測値として投入した量を、正式に廃棄として記録する。
     * ロット自体は健全なまま(needs_reviewは立てない)、投入分だけを無駄にしたケースに使う
     * (配合ミス・材料の不備・異物混入など)。
     */
    @PostMapping("/{lotId}/discard-usage")
    public ResponseEntity<Void> discardUsage(@PathVariable Long lotId, @RequestBody DiscardUsageRequest request) {
        materialLotService.discardUsage(lotId, request.getDiscardQty(), request.getReason(), request.getComment());
        return ResponseEntity.ok().build();
    }
}
