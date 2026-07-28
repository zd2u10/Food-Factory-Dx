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

/**
 * 在庫の手動調整(棚卸し等による補正)を扱うService。
 * material_lot.remainingQty を直接書き換えるAPIは用意せず、必ずこのServiceを経由させる。
 * これにより「いつ・なぜ在庫数が変わったか」を必ずstock_adjustmentに残す運用を強制する。
 */
@Service
public class StockAdjustmentService {

    private final MaterialLotMapper materialLotMapper;
    private final StockAdjustmentMapper stockAdjustmentMapper;

    public StockAdjustmentService(MaterialLotMapper materialLotMapper,
                                   StockAdjustmentMapper stockAdjustmentMapper) {
        this.materialLotMapper = materialLotMapper;
        this.stockAdjustmentMapper = stockAdjustmentMapper;
    }

    /**
     * ロットの残量を、実測した数量に補正する。
     * comment(理由)は必須とし、DDL側でもNOT NULLにしている。
     */
    @Transactional
    public StockAdjustment adjustLotQuantity(Long lotId, BigDecimal newQty, String comment) {
        MaterialLot lot = materialLotMapper.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたロットが見つかりません: lotId=" + lotId));

        BigDecimal beforeQty = lot.getRemainingQty();
        StockAdjustment adjustment = new StockAdjustment(lotId, beforeQty, newQty, LocalDate.now(), comment);
        stockAdjustmentMapper.insert(adjustment);

        materialLotMapper.setRemainingQty(lotId, newQty);

        return adjustment;
    }

    /** 特定のロットの調整履歴を全件取得する。 */
    public List<StockAdjustment> listByLotId(Long lotId) {
        return stockAdjustmentMapper.findByLotId(lotId);
    }
}
