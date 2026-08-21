package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.BatchOrderAllocation;
import com.foodfactory.dx.domain.CustomerOrder;
import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.domain.OrderLine;
import com.foodfactory.dx.mapper.BatchOrderAllocationMapper;
import com.foodfactory.dx.mapper.CustomerOrderMapper;
import com.foodfactory.dx.mapper.ManufacturingBatchMapper;
import com.foodfactory.dx.mapper.OrderLineMapper;
import com.foodfactory.dx.mapper.ShipmentLineMapper;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CustomerOrderService {

    private final CustomerOrderMapper customerOrderMapper;
    private final OrderLineMapper orderLineMapper;
    private final ShipmentLineMapper shipmentLineMapper;
    private final BatchOrderAllocationMapper batchOrderAllocationMapper;
    private final ManufacturingBatchMapper manufacturingBatchMapper;
    private final ManufacturingService manufacturingService;

    public CustomerOrderService(CustomerOrderMapper customerOrderMapper, OrderLineMapper orderLineMapper,
                                 ShipmentLineMapper shipmentLineMapper,
                                 BatchOrderAllocationMapper batchOrderAllocationMapper,
                                 ManufacturingBatchMapper manufacturingBatchMapper,
                                 ManufacturingService manufacturingService) {
        this.customerOrderMapper = customerOrderMapper;
        this.orderLineMapper = orderLineMapper;
        this.shipmentLineMapper = shipmentLineMapper;
        this.batchOrderAllocationMapper = batchOrderAllocationMapper;
        this.manufacturingBatchMapper = manufacturingBatchMapper;
        this.manufacturingService = manufacturingService;
    }

    public CustomerOrder createOrder(CustomerOrder order) {
        customerOrderMapper.insert(order);
        return order;
    }

    public List<CustomerOrder> listOrders() {
        return customerOrderMapper.findAll();
    }

    /**
     * 受注明細を1件登録する。
     * amount(金額)は、unitPriceが指定されていればqty×unitPriceをここで計算して保存する
     * (単価が後で変わっても、受注時点の金額を保持できるようにするため)。
     */
    public OrderLine createOrderLine(OrderLine line) {
        if (line.getUnitPrice() != null) {
            line.setAmount(line.getQty().multiply(line.getUnitPrice()));
        }
        orderLineMapper.insert(line);
        return line;
    }

    public List<OrderLine> listOrderLines(Long orderId) {
        return orderLineMapper.findByOrderId(orderId);
    }

    /**
     * 受注明細を編集する(商品・数量とも変更可能。取引先は受注ヘッダー側の項目のため、
     * この明細編集の対象外)。
     *
     * 出荷前の調整に限定する設計: 既にこの明細から出荷された実績がある場合、
     * その出荷済み数量を下回る変更(商品の差し替えを含む)は許可しない。
     * 記録として残っている出荷実績は、受注の編集では書き換えられないようにし、
     * もし取引先都合で出荷済み分が不要になった場合は、返品→在庫への手動追加という、
     * 既存の在庫調整の仕組みで対応する運用とする(この編集機能の範囲外)。
     */
    public OrderLine updateOrderLine(Long lineId, OrderLine line) {
        orderLineMapper.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注明細が見つかりません: lineId=" + lineId));

        BigDecimal shippedQty = shipmentLineMapper.sumShippedQtyByOrderLineId(lineId);
        if (line.getQty().compareTo(shippedQty) < 0) {
            throw new IllegalArgumentException(
                    "既に出荷済みの数量(" + shippedQty + ")を下回る数量には変更できません。"
                            + "指定された数量=" + line.getQty());
        }

        line.setLineId(lineId);
        if (line.getUnitPrice() != null) {
            line.setAmount(line.getQty().multiply(line.getUnitPrice()));
        }
        orderLineMapper.update(line);
        return line;
    }

    /** NEW → CONFIRMED。受注内容を確定する操作。 */
    public void confirmOrder(Long orderId) {
        CustomerOrder order = customerOrderMapper.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注が見つかりません: orderId=" + orderId));
        if (order.getStatus() != CustomerOrder.Status.NEW) {
            throw new IllegalStateException("NEW状態の受注のみ確定できます。現在の状態: " + order.getStatus());
        }
        customerOrderMapper.updateStatus(orderId, CustomerOrder.Status.CONFIRMED);
    }

    /** キャンセル。在庫プール型の設計のため、ステータス変更のみで在庫やバッチには一切手を加えない。 */
    public void cancelOrder(Long orderId) {
        CustomerOrder order = customerOrderMapper.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注が見つかりません: orderId=" + orderId));
        if (order.getStatus() == CustomerOrder.Status.COMPLETED
                || order.getStatus() == CustomerOrder.Status.CANCELLED) {
            throw new IllegalStateException("この受注はキャンセルできない状態です。現在の状態: " + order.getStatus());
        }
        customerOrderMapper.updateStatus(orderId, CustomerOrder.Status.CANCELLED);

        cancelOrphanedDraftBatches(orderId);
    }

    /**
     * 受注キャンセルによって、MRPが生成したDraftバッチのうち、
     * もう誰にも必要とされなくなったものを取り消す(案A、設計8.18節を参照)。
     *
     * 判定ロジック:
     *   このキャンセルされた受注に関連するバッチ(batch_order_allocation経由)を洗い出し、
     *   各バッチについて「まだ有効な受注に紐づく按分の合計」を再集計する。
     *   その合計が、バッチのplannedQtyを下回っている(=安全在庫由来の余地が残っている)場合、
     *   そのバッチは安全在庫として引き続き必要なので、キャンセルしない。
     *   合計がplannedQtyと一致する(=按分が全て有効な受注で埋まっている)場合のみ、
     *   バッチを取り消す。ただし、既にPLAN確定・製造中・完了しているバッチには手を出さない
     *   (DRAFT状態のもの、まだ何も手を付けていないものだけが対象)。
     *
     * 【注意】ここでの判定は「按分合計がplannedQtyを下回っているか」で行っているが、
     * これは正確には「安全在庫由来の枠が残っているか」の判定であり、キャンセルされた
     * 受注そのものの按分が0になったかどうかは見ていない。そのため、
     * 一部の受注だけがキャンセルされても、他の有効な受注や安全在庫の枠が
     * 残っている限り、バッチは維持される(要件通りの動作)。
     */
    private void cancelOrphanedDraftBatches(Long cancelledOrderId) {
        List<BatchOrderAllocation> allocations = batchOrderAllocationMapper.findByOrderId(cancelledOrderId);
        Set<Long> checkedBatchIds = new HashSet<>();

        for (BatchOrderAllocation allocation : allocations) {
            Long batchId = allocation.getBatchId();
            if (!checkedBatchIds.add(batchId)) {
                continue; // 同じバッチを二重にチェックしない
            }

            ManufacturingBatch batch = manufacturingBatchMapper.findById(batchId).orElse(null);
            if (batch == null || batch.getStatus() != ManufacturingBatch.Status.DRAFT) {
                continue; // 既にPLAN確定以降に進んでいるバッチには手を出さない
            }

            // 安全在庫由来の余地があるかどうかは、「MRPが元々このバッチに割り当てた、
            // 全ての受注按分の合計」(キャンセル済みの受注分も含む、全件)で判定する必要がある。
            // ここでキャンセル済みを除外した「有効な按分」だけを見てしまうと、
            // 「A・B両方キャンセルされたら、安全在庫分128も一緒に消えてしまう」という
            // 不具合が起きる(全ての受注が消えれば、有効按分は必ず0になるため)。
            List<BatchOrderAllocation> allAllocationsForBatch = batchOrderAllocationMapper.findByBatchId(batchId);
            BigDecimal totalAllocatedEver = allAllocationsForBatch.stream()
                    .map(BatchOrderAllocation::getAllocatedQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            boolean hasSafetyStockSlack = totalAllocatedEver.compareTo(batch.getPlannedQty()) < 0;
            if (hasSafetyStockSlack) {
                continue; // 安全在庫由来の枠が最初から確保されているバッチは、常に維持する
            }

            // ここまで来たバッチは「安全在庫の余地が無い、完全に受注由来のバッチ」。
            // まだ有効な(キャンセルされていない)受注が1件でも残っていれば維持し、
            // 全て無くなった場合のみ取り消す。
            BigDecimal activeAllocatedQty = batchOrderAllocationMapper.sumActiveAllocatedQtyByBatchId(batchId);
            if (activeAllocatedQty.compareTo(BigDecimal.ZERO) > 0) {
                continue;
            }

            manufacturingService.cancelBatch(batchId, "受注ID" + cancelledOrderId + "のキャンセルに伴う自動取り消し");
        }
    }
}
