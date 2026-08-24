package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.MaterialLot;
import com.foodfactory.dx.domain.StockAdjustment;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import com.foodfactory.dx.mapper.StockAdjustmentMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 材料ロットの「要確認」状態、および製造実行画面での「破棄する」操作に関するService。
 *
 * 【背景】製造実行画面で、劣化・配合ミスなどが疑われるロットを「別ロットに
 * 切り替える」操作を行うと、そのロットは needs_review=true になり、以降の
 * FEFO自動選定から除外される(判断は保留し、残量自体は変更しない)。
 * 人が実際に検査を行い、その結果(生存量、または全量破棄)をこのServiceで
 * 確定させることで、初めて在庫データが正式に更新される(要件定義書8.21節を参照)。
 */
@Service
public class MaterialLotService {

    private final MaterialLotMapper materialLotMapper;
    private final StockAdjustmentMapper stockAdjustmentMapper;

    public MaterialLotService(MaterialLotMapper materialLotMapper, StockAdjustmentMapper stockAdjustmentMapper) {
        this.materialLotMapper = materialLotMapper;
        this.stockAdjustmentMapper = stockAdjustmentMapper;
    }

    /** 要確認フラグが立っている、全ロットを取得する(在庫画面の要確認セクション用)。 */
    public List<MaterialLot> listNeedsReview() {
        return materialLotMapper.findNeedsReview();
    }

    /**
     * 検査結果を登録する(生存量方式)。
     * 生存量をそのまま新しい残量として確定させ、要確認フラグを解除する。
     * 廃棄された分(元の残量 - 生存量)は、stock_adjustmentに記録する。
     *
     * survivingQty = 0 を渡せば、実質「全量破棄」と同じ結果になる
     * (呼び出し側で、全量破棄専用のUIを分けるかどうかは自由)。
     */
    @Transactional
    public void resolveReview(Long lotId, BigDecimal survivingQty,
                               StockAdjustment.StockReviewReason reason, String comment) {
        MaterialLot lot = materialLotMapper.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("指定された材料ロットが見つかりません: lotId=" + lotId));
        if (!lot.isNeedsReview()) {
            throw new IllegalStateException("このロットは要確認状態ではありません。lotId=" + lotId);
        }
        if (survivingQty.compareTo(lot.getRemainingQty()) > 0) {
            throw new IllegalArgumentException(
                    "生存量は、要確認になった時点の残量(" + lot.getRemainingQty() + ")を超えることはできません。指定値=" + survivingQty);
        }
        validateOtherReasonHasComment(reason == StockAdjustment.StockReviewReason.OTHER, comment);

        BigDecimal beforeQty = lot.getRemainingQty();
        stockAdjustmentMapper.insert(
                StockAdjustment.forStockReview(lotId, beforeQty, survivingQty, LocalDate.now(), reason, comment));
        materialLotMapper.resolveReview(lotId, survivingQty);
    }

    /**
     * 製造実行画面の「破棄する」操作。実測値として投入した量を、正式に廃棄として記録する。
     *
     * 【「別ロットに切り替える」との違い】こちらは、ロット自体(1袋全体)を疑うのではなく、
     * 「今回、投入した分(計量ミス・配合ミスなど)だけを無駄にした」というケースに使う。
     * そのため needs_review は立てず、即座に remaining_qty を正式に減算する
     * (ロット自体は健全なので、引き続きFEFOの対象で問題ない)。
     */
    @Transactional
    public void discardUsage(Long lotId, BigDecimal discardQty,
                              StockAdjustment.UsageDiscardReason reason, String comment) {
        MaterialLot lot = materialLotMapper.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("指定された材料ロットが見つかりません: lotId=" + lotId));
        if (discardQty.compareTo(lot.getRemainingQty()) > 0) {
            throw new IllegalArgumentException(
                    "廃棄量は、現在の残量(" + lot.getRemainingQty() + ")を超えることはできません。指定値=" + discardQty);
        }
        validateOtherReasonHasComment(reason == StockAdjustment.UsageDiscardReason.OTHER, comment);

        BigDecimal beforeQty = lot.getRemainingQty();
        BigDecimal afterQty = beforeQty.subtract(discardQty);
        stockAdjustmentMapper.insert(
                StockAdjustment.forUsageDiscard(lotId, beforeQty, afterQty, LocalDate.now(), reason, comment));
        materialLotMapper.decrementRemainingQty(lotId, discardQty);
    }

    /**
     * 理由が「その他」の場合、詳細な説明(comment)が空でないことを検証する。
     * 「その他」を選んだのに詳細説明が空、という記録として意味の薄いデータを防ぐ。
     */
    private void validateOtherReasonHasComment(boolean isOtherReason, String comment) {
        if (isOtherReason && !StringUtils.hasText(comment)) {
            throw new IllegalArgumentException("理由が「その他」の場合は、具体的な内容をコメントに入力してください。");
        }
    }
}
