package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Item;
import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.domain.MrpRun;
import com.foodfactory.dx.domain.OrderLine;
import com.foodfactory.dx.mapper.BatchOrderAllocationMapper;
import com.foodfactory.dx.mapper.ItemMapper;
import com.foodfactory.dx.mapper.ManufacturingBatchMapper;
import com.foodfactory.dx.mapper.MrpRunMapper;
import com.foodfactory.dx.mapper.OrderLineMapper;
import com.foodfactory.dx.mapper.ShipmentLineMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MRP(資材所要量計画の考え方を商品の生産計画に応用したもの)自動化のロジック。
 *
 * 計算式(要件定義書 5.2節を参照。二重控除を修正した最新版):
 *   需要量     = 受注残 + 適正在庫(そのまま、最終的に残しておきたい水準として)
 *   供給予定量 = DRAFT/PLAN/MANUFACTURING状態のバッチのplannedQty合計
 *   正味不足量 = 需要量 − (有効在庫 + 供給予定量)
 *
 * 正味不足量が0より大きい場合のみ、標準バッチ数量(standardBatchQty)単位で
 * 切り上げてDraftを生成する(縮小バッチは作らない。フェーズ0で確定した方針)。
 *
 * 実行のきっかけ(triggeredBy)は3種類:
 *   AUTO   : 1日1回の定期実行(将来的にスケジューラを組む想定。今回は手動でAPIを叩く形で代用)
 *   MANUAL : 人がボタンを押して実行
 *   EVENT  : CANCELLED/REJECTED発生時、ManufacturingServiceから即座に呼ばれる
 */
@Service
public class MrpService {

    private final ItemMapper itemMapper;
    private final OrderLineMapper orderLineMapper;
    private final ShipmentLineMapper shipmentLineMapper;
    private final ManufacturingBatchMapper manufacturingBatchMapper;
    private final MrpRunMapper mrpRunMapper;
    private final ManufacturingService manufacturingService;
    private final BatchOrderAllocationMapper batchOrderAllocationMapper;

    public MrpService(ItemMapper itemMapper,
                       OrderLineMapper orderLineMapper,
                       ShipmentLineMapper shipmentLineMapper,
                       ManufacturingBatchMapper manufacturingBatchMapper,
                       MrpRunMapper mrpRunMapper,
                       ManufacturingService manufacturingService,
                       BatchOrderAllocationMapper batchOrderAllocationMapper) {
        this.itemMapper = itemMapper;
        this.orderLineMapper = orderLineMapper;
        this.shipmentLineMapper = shipmentLineMapper;
        this.manufacturingBatchMapper = manufacturingBatchMapper;
        this.mrpRunMapper = mrpRunMapper;
        this.manufacturingService = manufacturingService;
        this.batchOrderAllocationMapper = batchOrderAllocationMapper;
    }

    /**
     * 全商品についてMRPを実行する。1回の実行につき mrp_run を1件記録し、
     * 生成された全てのDraftバッチに、その実行のrunIdを紐付ける。
     */
    @Transactional
    public List<ManufacturingBatch> runForAllItems(MrpRun.TriggeredBy triggeredBy) {
        MrpRun run = new MrpRun(triggeredBy);
        mrpRunMapper.insert(run);

        List<ManufacturingBatch> created = new ArrayList<>();
        for (Item item : itemMapper.findAll()) {
            created.addAll(runForItemInternal(item, run.getRunId()));
        }
        return created;
    }

    /**
     * 特定の商品1点についてだけMRPを実行する。
     * CANCELLED/REJECTED発生時のEVENTトリガーは、全商品を再計算する必要はなく、
     * 影響を受けた商品だけ見れば十分なため、こちらの単品版を用意している。
     */
    @Transactional
    public List<ManufacturingBatch> runForItem(Long itemId, MrpRun.TriggeredBy triggeredBy) {
        Item item = itemMapper.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません: itemId=" + itemId));

        MrpRun run = new MrpRun(triggeredBy);
        mrpRunMapper.insert(run);

        return runForItemInternal(item, run.getRunId());
    }

    private List<ManufacturingBatch> runForItemInternal(Item item, Long runId) {
        Long itemId = item.getItemId();

        // 受注残 = 注文された総数(CANCELLED以外) − 既に出荷済みの数
        BigDecimal totalOrdered = orderLineMapper.sumActiveQtyByItemId(itemId);
        BigDecimal totalShipped = shipmentLineMapper.sumShippedQtyByItemId(itemId);
        BigDecimal backlog = totalOrdered.subtract(totalShipped).max(BigDecimal.ZERO);

        // 有効在庫 = 完成済み(COMPLETED)バッチの残量合計
        BigDecimal effectiveStock = manufacturingBatchMapper.sumRemainingQtyByItemId(itemId);

        // 供給予定量 = DRAFT/PLAN/MANUFACTURING状態のバッチのplannedQty合計
        BigDecimal supplyPool = manufacturingBatchMapper.sumPlannedQtyByItemId(itemId);

        BigDecimal demand = backlog.add(item.getSafetyStockQty());
        BigDecimal netShortage = demand.subtract(effectiveStock.add(supplyPool));

        List<ManufacturingBatch> created = new ArrayList<>();
        if (netShortage.compareTo(BigDecimal.ZERO) <= 0) {
            return created; // 不足なし。何も作らない
        }

        // 標準バッチ数量単位で切り上げる(縮小バッチは作らない)。
        // BigDecimalの割り算はscaleを指定しないと例外になるため、RoundingMode.CEILINGを指定して
        // 「割り切れない場合は切り上げる」動きにしている。
        BigDecimal standardBatchQty = item.getStandardBatchQty();
        BigDecimal batchCountDecimal = netShortage.divide(standardBatchQty, 0, RoundingMode.CEILING);
        int batchCount = batchCountDecimal.intValue();

        LocalDate today = LocalDate.now();
        for (int i = 0; i < batchCount; i++) {
            ManufacturingBatch batch = manufacturingService.createAutoBatch(itemId, today, runId);
            created.add(batch);
        }

        allocateOrdersToBatches(itemId, created);

        return created;
    }

    /**
     * 生成されたバッチ群に、まだ出荷されていない受注(古い順)を、先頭のバッチから順に
     * 按分して記録する。1個でも受注由来の数量を含むバッチには、その受注の按分を作る。
     * 按分しきれなかった残り(バッチ容量 - 受注按分合計)は、安全在庫由来として、
     * レコードを作らずそのままにする(暗黙的な表現)。
     *
     * 【注意】この按分は「受注をどのバッチに割り当てたか」の記録用であり、
     * 実際にどのバッチからどの受注へ出荷するか(FEFO)を決めるものではない。
     * 出荷時のバッチ選定は、従来通りShipmentService側のFEFOロジックが行う。
     */
    private void allocateOrdersToBatches(Long itemId, List<ManufacturingBatch> createdBatches) {
        if (createdBatches.isEmpty()) {
            return;
        }

        // まだ出荷されていない、有効な受注明細を、受注日が古い順(先に受けた注文を優先)に取得する。
        List<OrderLine> activeLines = orderLineMapper.findActiveLinesByItemIdOrderByOrderDate(itemId);

        int batchIndex = 0;
        BigDecimal remainingInCurrentBatch = createdBatches.get(0).getPlannedQty();

        for (OrderLine line : activeLines) {
            BigDecimal shipped = shipmentLineMapper.sumShippedQtyByOrderLineId(line.getLineId());
            BigDecimal remainingInLine = line.getQty().subtract(shipped).max(BigDecimal.ZERO);
            if (remainingInLine.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 既に出荷済みで残量が無い明細は、按分の対象外
            }

            while (remainingInLine.compareTo(BigDecimal.ZERO) > 0 && batchIndex < createdBatches.size()) {
                BigDecimal allocateQty = remainingInLine.min(remainingInCurrentBatch);
                if (allocateQty.compareTo(BigDecimal.ZERO) > 0) {
                    batchOrderAllocationMapper.insert(new com.foodfactory.dx.domain.BatchOrderAllocation(
                            createdBatches.get(batchIndex).getBatchId(), line.getOrderId(), allocateQty));
                    remainingInLine = remainingInLine.subtract(allocateQty);
                    remainingInCurrentBatch = remainingInCurrentBatch.subtract(allocateQty);
                }
                if (remainingInCurrentBatch.compareTo(BigDecimal.ZERO) <= 0) {
                    batchIndex += 1;
                    if (batchIndex < createdBatches.size()) {
                        remainingInCurrentBatch = createdBatches.get(batchIndex).getPlannedQty();
                    }
                }
            }
            if (batchIndex >= createdBatches.size()) {
                break; // 全バッチを受注で使い切った(残りは安全在庫の必要量分のみ生成されている想定)
            }
        }
    }
}
