package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Customer;
import com.foodfactory.dx.domain.CustomerOrder;
import com.foodfactory.dx.domain.Item;
import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.domain.OrderLine;
import com.foodfactory.dx.domain.Shipment;
import com.foodfactory.dx.domain.ShipmentLine;
import com.foodfactory.dx.dto.BatchAllocationInput;
import com.foodfactory.dx.dto.ShipmentAllocationLine;
import com.foodfactory.dx.dto.ShipmentAllocationResult;
import com.foodfactory.dx.mapper.CustomerMapper;
import com.foodfactory.dx.mapper.CustomerOrderMapper;
import com.foodfactory.dx.mapper.ItemMapper;
import com.foodfactory.dx.mapper.ManufacturingBatchMapper;
import com.foodfactory.dx.mapper.OrderLineMapper;
import com.foodfactory.dx.mapper.ShipmentLineMapper;
import com.foodfactory.dx.mapper.ShipmentMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 出荷管理(フェーズ5)の中核ロジック。
 *
 * 材料側のFEFO(ManufacturingService.previewFefoAllocation)と考え方は同じだが、
 * 商品の出荷では追加で「取引先ごとの残存期限ルール」という制約が加わる。
 * 許可産地の代替を認めなかった材料側の方針と同様、
 * 残存期限ルールを満たさないバッチを現場判断で代替出荷することは許可しない
 * (要件定義書 3.2節の産地制約の方針、3.1節の残存期限ルールを踏襲)。
 *
 * 【残存期限ルールは「割合(%)」ではなく「日数」で判定する】
 * 以前は取引先ごとに割合(0〜1)を持たせていたが、現場が実際に判断する基準は
 * 「あと何日残っているか」であり、割合は商品ごとに賞味期限日数が違うと
 * 都度換算が必要で直感的でなかったため、日数ベースの判定に変更した(要件定義書 8.9節)。
 */
@Service
public class ShipmentService {

    private final ShipmentMapper shipmentMapper;
    private final ShipmentLineMapper shipmentLineMapper;
    private final OrderLineMapper orderLineMapper;
    private final CustomerOrderMapper customerOrderMapper;
    private final CustomerMapper customerMapper;
    private final ItemMapper itemMapper;
    private final ManufacturingBatchMapper manufacturingBatchMapper;

    public ShipmentService(ShipmentMapper shipmentMapper,
                            ShipmentLineMapper shipmentLineMapper,
                            OrderLineMapper orderLineMapper,
                            CustomerOrderMapper customerOrderMapper,
                            CustomerMapper customerMapper,
                            ItemMapper itemMapper,
                            ManufacturingBatchMapper manufacturingBatchMapper) {
        this.shipmentMapper = shipmentMapper;
        this.shipmentLineMapper = shipmentLineMapper;
        this.orderLineMapper = orderLineMapper;
        this.customerOrderMapper = customerOrderMapper;
        this.customerMapper = customerMapper;
        this.itemMapper = itemMapper;
        this.manufacturingBatchMapper = manufacturingBatchMapper;
    }

    /** 出荷ヘッダー(配送1回分)を登録する。 */
    public Shipment createShipment(Shipment shipment) {
        shipmentMapper.insert(shipment);
        return shipment;
    }

    public List<Shipment> listShipments() {
        return shipmentMapper.findAll();
    }

    /**
     * 指定した受注明細について、まだ出荷していない残数量分を、FEFO(製造日が古い順)で自動選定する。
     * このメソッド自体はDBを変更しない、あくまで計算結果のプレビュー。
     */
    public ShipmentAllocationResult previewShipmentAllocation(Long orderLineId) {
        OrderLine orderLine = orderLineMapper.findById(orderLineId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注明細が見つかりません: orderLineId=" + orderLineId));
        CustomerOrder order = customerOrderMapper.findById(orderLine.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("指定された受注が見つかりません: orderId=" + orderLine.getOrderId()));
        Customer customer = customerMapper.findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("指定された取引先が見つかりません: customerId=" + order.getCustomerId()));
        Item item = itemMapper.findById(orderLine.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません: itemId=" + orderLine.getItemId()));

        BigDecimal alreadyShipped = shipmentLineMapper.sumShippedQtyByOrderLineId(orderLineId);
        BigDecimal remainingNeed = orderLine.getQty().subtract(alreadyShipped);

        ShipmentAllocationResult result = new ShipmentAllocationResult();
        if (remainingNeed.compareTo(BigDecimal.ZERO) <= 0) {
            return result; // 既に出荷完了している場合は空の結果を返す
        }

        List<ManufacturingBatch> candidates =
                manufacturingBatchMapper.findShippableByItemIdOrderByBatchDate(orderLine.getItemId());

        for (ManufacturingBatch batch : candidates) {
            if (remainingNeed.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // 残存日数を計算する(computeResidualDaysは登録処理側とも共通)。
            int residualDays = computeResidualDays(batch, item.getShelfLifeDays());

            // 取引先が残存期限の日数を指定している場合、それを下回るバッチは候補から除外する
            // (現場判断での代替は許可しない。要件定義書の産地制約と同じ方針)。
            if (customer.getRequiredResidualDays() != null
                    && residualDays < customer.getRequiredResidualDays()) {
                continue;
            }

            BigDecimal allocate = batch.getRemainingQty().min(remainingNeed);
            result.getLines().add(new ShipmentAllocationLine(
                    batch.getBatchId(), batch.getBatchDate(), residualDays, allocate));
            remainingNeed = remainingNeed.subtract(allocate);
        }

        if (remainingNeed.compareTo(BigDecimal.ZERO) > 0) {
            result.setShortage(true);
        }

        return result;
    }

    /**
     * 出荷明細を登録し、対応するバッチの残量を減らす。
     * 複数バッチにまたがる出荷(1つの受注明細を複数バッチから出荷する)に対応するため、
     * allocations(バッチごとの出荷数量のリスト)を受け取る。
     *
     * 【重要】previewShipmentAllocation(プレビュー)で行っている残存期限ルールの判定を、
     * この登録処理でも必ず同様に行う。プレビューだけでチェックし、実際の登録処理では
     * チェックしない、という作りにすると、プレビューの警告を無視してそのまま
     * 不適合なバッチを登録できてしまう抜け穴になるため。
     */
    @Transactional
    public void registerShipmentLines(Long shipmentId, Long orderLineId, List<BatchAllocationInput> allocations) {
        shipmentMapper.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("指定された出荷ヘッダーが見つかりません: shipmentId=" + shipmentId));
        OrderLine orderLine = orderLineMapper.findById(orderLineId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注明細が見つかりません: orderLineId=" + orderLineId));

        CustomerOrder order = customerOrderMapper.findById(orderLine.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("指定された受注が見つかりません: orderId=" + orderLine.getOrderId()));
        Customer customer = customerMapper.findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("指定された取引先が見つかりません: customerId=" + order.getCustomerId()));
        Item item = itemMapper.findById(orderLine.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません: itemId=" + orderLine.getItemId()));

        for (BatchAllocationInput allocation : allocations) {
            ManufacturingBatch batch = manufacturingBatchMapper.findById(allocation.getBatchId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "指定されたバッチが見つかりません: batchId=" + allocation.getBatchId()));

            if (!batch.getItemId().equals(orderLine.getItemId())) {
                throw new IllegalArgumentException(
                        "受注明細の商品(itemId=" + orderLine.getItemId() + ")と、"
                                + "バッチの商品(itemId=" + batch.getItemId() + ")が一致しません。");
            }
            if (batch.getStatus() != ManufacturingBatch.Status.COMPLETED) {
                throw new IllegalStateException(
                        "COMPLETED状態のバッチのみ出荷できます。batchId=" + batch.getBatchId()
                                + ", 現在の状態=" + batch.getStatus());
            }

            // 残存期限ルールの検証。プレビューと全く同じ計算式を使う(computeResidualDays)。
            // 現場判断での代替は許可しないため、満たさない場合は登録そのものをブロックする。
            if (customer.getRequiredResidualDays() != null) {
                int residualDays = computeResidualDays(batch, item.getShelfLifeDays());
                if (residualDays < customer.getRequiredResidualDays()) {
                    throw new IllegalArgumentException(
                            "このバッチ(batchId=" + batch.getBatchId() + ")は取引先の残存期限ルール"
                                    + "(" + customer.getRequiredResidualDays() + "日以上)を満たしていません。"
                                    + "残存日数=" + residualDays + "日");
                }
            }

            int updatedRows = manufacturingBatchMapper.decrementRemainingQty(
                    batch.getBatchId(), allocation.getShippedQty());
            if (updatedRows == 0) {
                throw new IllegalArgumentException(
                        "バッチの在庫を確保できませんでした(残量不足、または他の処理と競合した可能性があります): batchId="
                                + batch.getBatchId());
            }

            shipmentLineMapper.insert(new ShipmentLine(
                    shipmentId, orderLineId, batch.getBatchId(), allocation.getShippedQty()));
        }

        recalculateOrderStatus(orderLine.getOrderId());
    }

    /**
     * バッチの残存期限の日数を計算する: 賞味期限日数 - 経過日数。
     * previewShipmentAllocationとregisterShipmentLinesの両方から呼ばれる共通ロジック
     * (プレビューと登録時の判定基準が食い違うことを防ぐため、計算式を1箇所にまとめている)。
     */
    private int computeResidualDays(ManufacturingBatch batch, int shelfLifeDays) {
        long elapsedDays = ChronoUnit.DAYS.between(batch.getBatchDate(), LocalDate.now());
        return (int) (shelfLifeDays - elapsedDays);
    }

    /**
     * 受注全体の充足状況を再集計し、ステータスを更新する。
     * CANCELLED状態の受注は対象外とし、ステータスを勝手に書き換えない。
     */
    private void recalculateOrderStatus(Long orderId) {
        CustomerOrder order = customerOrderMapper.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注が見つかりません: orderId=" + orderId));
        if (order.getStatus() == CustomerOrder.Status.CANCELLED) {
            return;
        }

        BigDecimal totalOrdered = orderLineMapper.sumQtyByOrderId(orderId);
        BigDecimal totalShipped = shipmentLineMapper.sumShippedQtyByOrderId(orderId);

        CustomerOrder.Status newStatus;
        if (totalShipped.compareTo(totalOrdered) >= 0) {
            newStatus = CustomerOrder.Status.COMPLETED;
        } else if (totalShipped.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = CustomerOrder.Status.PARTIALLY_SHIPPED;
        } else {
            // まだ何も出荷されていない場合は、現在のステータス(NEW/CONFIRMED)を維持する
            return;
        }

        customerOrderMapper.updateStatus(orderId, newStatus);
    }
}
