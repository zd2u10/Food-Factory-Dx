package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.HoldResolution;
import com.foodfactory.dx.domain.MaterialArrivalLine;
import com.foodfactory.dx.domain.MaterialLot;
import com.foodfactory.dx.domain.StockAdjustment;
import com.foodfactory.dx.mapper.HoldResolutionMapper;
import com.foodfactory.dx.mapper.MaterialArrivalLineMapper;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import com.foodfactory.dx.mapper.StockAdjustmentMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 保留(hold_resolution)の対応(返品・結局受け入れ)を扱うService。
 * 「交換」による対応は、新しい入荷明細の登録そのものなので ProcurementService 側にある
 * (ProcurementService.registerInspectedLine(line, resolvesHoldId))。
 */
@Service
public class HoldResolutionService {

    private final HoldResolutionMapper holdResolutionMapper;
    private final MaterialArrivalLineMapper materialArrivalLineMapper;
    private final MaterialLotMapper materialLotMapper;
    private final StockAdjustmentMapper stockAdjustmentMapper;
    private final ProcurementService procurementService;

    public HoldResolutionService(HoldResolutionMapper holdResolutionMapper,
                                  MaterialArrivalLineMapper materialArrivalLineMapper,
                                  MaterialLotMapper materialLotMapper,
                                  StockAdjustmentMapper stockAdjustmentMapper,
                                  ProcurementService procurementService) {
        this.holdResolutionMapper = holdResolutionMapper;
        this.materialArrivalLineMapper = materialArrivalLineMapper;
        this.materialLotMapper = materialLotMapper;
        this.stockAdjustmentMapper = stockAdjustmentMapper;
        this.procurementService = procurementService;
    }

    /** 対応待ち(ON_HOLD)の保留を全件取得する。 */
    public List<HoldResolution> listOpenHolds() {
        return holdResolutionMapper.findByStatus(HoldResolution.Status.ON_HOLD);
    }

    /** ステータス問わず全件取得する。監査・トレーサビリティ確認用。 */
    public List<HoldResolution> listAllHolds() {
        return holdResolutionMapper.findAll();
    }

    /** 指定した発注に関わった保留の履歴を、ステータス問わず全件取得する(発注詳細画面での表示用)。 */
    public List<HoldResolution> listHoldsByOrderId(Long orderId) {
        return holdResolutionMapper.findByOrderId(orderId);
    }

    /**
     * 返品として対応する。自社に問題がない仕入先都合の返却であり、在庫は一切増減しない
     * (そもそも合格していない分なので、在庫に反映されたことが無い)。
     */
    @Transactional
    public void resolveAsReturned(Long holdId, String comment) {
        HoldResolution hold = getHoldOrThrow(holdId);
        holdResolutionMapper.resolve(holdId, HoldResolution.ResolutionType.RETURNED, null, comment);
    }

    /**
     * 結局受け入れる、として対応する。
     * 保留になっていた数量を、元の入荷明細のacceptedQtyに繰り入れ、
     * 対応する材料ロットの残量を増やす(無ければ新規作成する)。
     *
     * 在庫が増える際は、必ず stock_adjustment に調整前後の値を記録してから反映する
     * (在庫が理由なく増減した記録が残らない、という事態を避けるため)。
     */
    @Transactional
    public void resolveAsAcceptedLate(Long holdId, String comment) {
        HoldResolution hold = getHoldOrThrow(holdId);
        BigDecimal qty = hold.getHeldQtySnapshot();

        MaterialArrivalLine line = materialArrivalLineMapper.findById(hold.getLineId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "元の入荷明細が見つかりません: lineId=" + hold.getLineId()));

        // 元の明細のacceptedQty/heldQtyを更新する(保留分を合格分へ繰り入れる)。
        // 検品チェック項目(破損・期限切れ・異物混入)は変わらないため、そのまま引き継いで渡す。
        line.setAcceptedQty(line.getAcceptedQty().add(qty));
        line.setHeldQty(BigDecimal.ZERO);
        materialArrivalLineMapper.updateInspectionResult(line);

        // 対応する材料ロットが既にあるか確認する(1明細につきロットは1件までの制約があるため)。
        MaterialLot existingLot = materialLotMapper.findByArrivalLineId(line.getLineId()).orElse(null);

        LocalDate adjustmentDate = LocalDate.now();
        String adjustmentComment = "hold_id=" + holdId + " の保留対応(ACCEPTED_LATE)による在庫増加";

        if (existingLot != null) {
            // 既存ロットがある場合(同じ明細内で一部合格・一部保留だったケース) → 残量を増やす
            BigDecimal beforeQty = existingLot.getRemainingQty();
            BigDecimal afterQty = beforeQty.add(qty);
            stockAdjustmentMapper.insert(
                    new StockAdjustment(existingLot.getLotId(), beforeQty, afterQty, adjustmentDate, adjustmentComment));
            materialLotMapper.incrementRemainingQty(existingLot.getLotId(), qty);
        } else {
            // 既存ロットが無い場合(全量保留で、合格分ゼロだったケース) → 新規にロットを作成する
            MaterialLot newLot = new MaterialLot(
                    line.getMaterialId(), line.getLineId(), line.getSupplierLotNo(),
                    line.getOrigin(), line.getExpiryDate(), qty);
            materialLotMapper.insert(newLot);
            stockAdjustmentMapper.insert(
                    new StockAdjustment(newLot.getLotId(), BigDecimal.ZERO, qty, adjustmentDate, adjustmentComment));
        }

        // この明細が発注に紐づく場合、合格数量が増えたことで発注の充足状況も変わるため再集計する。
        if (line.getOrderId() != null) {
            procurementService.recalculateOrderStatus(line.getOrderId());
        }

        holdResolutionMapper.resolve(holdId, HoldResolution.ResolutionType.ACCEPTED_LATE, null, comment);
    }

    private HoldResolution getHoldOrThrow(Long holdId) {
        HoldResolution hold = holdResolutionMapper.findById(holdId)
                .orElseThrow(() -> new IllegalArgumentException("指定された保留が見つかりません: holdId=" + holdId));
        if (hold.getStatus() != HoldResolution.Status.ON_HOLD) {
            throw new IllegalStateException("この保留は既に対応済みです。holdId=" + holdId);
        }
        return hold;
    }
}
