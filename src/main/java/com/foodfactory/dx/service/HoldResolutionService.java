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
    /**
     * 保留を「結局受け入れる」として対応する。
     *
     * 【設計変更】以前は、元の明細(material_arrival_line)のheld_qtyをaccepted_qtyに
     * 繰り入れ、既存ロットに残量を加算する(または無ければ新規作成する)方式だった。
     * この方式では、「普通に合格した分」と「一度保留を経て受け入れた分」が
     * 同じロットに混ざってしまい、現場が実物にシールで印を付けて区別している実態と
     * システムのデータが一致しなくなる、というトレーサビリティ上の問題があった。
     *
     * 新しい設計では、元の明細のaccepted_qty/held_qtyは一切書き換えず
     * (「保留が発生した」という記録として、そのまま残す)、代わりに
     * 常に新しいロットを1件作成し、そのロットにorigin_hold_idとして
     * この保留のIDを記録する。これにより、ロット単位で「結局受け入れ」経由かどうかを
     * 追跡できるようになる(要件定義書8.17節を参照)。
     */
    public void resolveAsAcceptedLate(Long holdId, String comment) {
        HoldResolution hold = getHoldOrThrow(holdId);
        BigDecimal qty = hold.getHeldQtySnapshot();

        MaterialArrivalLine line = materialArrivalLineMapper.findById(hold.getLineId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "元の入荷明細が見つかりません: lineId=" + hold.getLineId()));

        // 元の明細(accepted_qty/held_qty)は書き換えない。保留が発生したという記録を
        // そのまま残すため。産地・賞味期限は、この明細からそのまま引き継ぐ。
        MaterialLot newLot = new MaterialLot(
                line.getMaterialId(), line.getLineId(), line.getSupplierLotNo(),
                line.getOrigin(), line.getExpiryDate(), qty);
        newLot.setOriginHoldId(holdId);
        materialLotMapper.insert(newLot);

        LocalDate adjustmentDate = LocalDate.now();
        String adjustmentComment = "hold_id=" + holdId + " の保留対応(ACCEPTED_LATE)による在庫増加(新規ロット)";
        stockAdjustmentMapper.insert(
                new StockAdjustment(newLot.getLotId(), BigDecimal.ZERO, qty, adjustmentDate, adjustmentComment));

        // この明細が発注に紐づく場合、充足状況が変わるため再集計する。
        // 充足率計算自体は、新しく作られたロット(origin_hold_id経由)を
        // MaterialOrderService側で合算する形に変更済み(sumAcceptedLateQtyByOrderId)。
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
