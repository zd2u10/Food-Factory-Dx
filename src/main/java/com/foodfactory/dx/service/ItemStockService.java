package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.ItemStockAdjustment;
import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.mapper.ItemStockAdjustmentMapper;
import com.foodfactory.dx.mapper.ManufacturingBatchMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商品在庫(COMPLETED状態のmanufacturing_batch)に対する、バックヤード担当者の
 * 手動調整(廃棄)に関するService。
 *
 * 【対象範囲】COMPLETED(検品完了・在庫化済み)の商品ロットのみを対象とする。
 * MANUFACTURING中の重大な異常による廃棄は、製造現場の既存機能
 * (ManufacturingService.rejectBatch)で引き続き扱う(要件定義書8.25節を参照)。
 */
@Service
public class ItemStockService {

    private final ManufacturingBatchMapper manufacturingBatchMapper;
    private final ItemStockAdjustmentMapper itemStockAdjustmentMapper;

    public ItemStockService(ManufacturingBatchMapper manufacturingBatchMapper,
                             ItemStockAdjustmentMapper itemStockAdjustmentMapper) {
        this.manufacturingBatchMapper = manufacturingBatchMapper;
        this.itemStockAdjustmentMapper = itemStockAdjustmentMapper;
    }

    /**
     * COMPLETED状態の商品ロットを、指定した量だけ廃棄する。
     * 廃棄量は、そのロットの現在の残量を超えることはできない。
     */
    @Transactional
    public void discardItemStock(Long batchId, BigDecimal discardQty,
                                  ItemStockAdjustment.AdjustmentReason reason, String comment) {
        ManufacturingBatch batch = manufacturingBatchMapper.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたバッチが見つかりません: batchId=" + batchId));
        if (batch.getStatus() != ManufacturingBatch.Status.COMPLETED) {
            throw new IllegalStateException(
                    "COMPLETED状態の商品ロットのみ廃棄できます。現在の状態: " + batch.getStatus());
        }
        if (discardQty.compareTo(batch.getRemainingQty()) > 0) {
            throw new IllegalArgumentException(
                    "廃棄量は、現在の残量(" + batch.getRemainingQty() + ")を超えることはできません。指定値=" + discardQty);
        }
        if (reason == ItemStockAdjustment.AdjustmentReason.OTHER && !StringUtils.hasText(comment)) {
            throw new IllegalArgumentException("理由が「その他」の場合は、具体的な内容をコメントに入力してください。");
        }

        BigDecimal beforeQty = batch.getRemainingQty();
        BigDecimal afterQty = beforeQty.subtract(discardQty);
        itemStockAdjustmentMapper.insert(
                new ItemStockAdjustment(batchId, beforeQty, afterQty, LocalDate.now(), reason, comment));
        manufacturingBatchMapper.decrementRemainingQty(batchId, discardQty);
    }

    public List<ItemStockAdjustment> listAdjustments(Long batchId) {
        return itemStockAdjustmentMapper.findByBatchId(batchId);
    }
}
