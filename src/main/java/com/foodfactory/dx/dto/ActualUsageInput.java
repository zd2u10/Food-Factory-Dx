package com.foodfactory.dx.dto;

import java.math.BigDecimal;

/**
 * 製造実行(execute)のリクエストボディで使うDTO。
 * 「どのロットから、実際に何g/ml使ったか」を作業員が入力した値をそのまま受け取る。
 * suggestedQty(理論値)はService側でFEFO計算をやり直して補完するため、
 * ここでは受け取らない(理論値は常にサーバー側の計算結果を信頼する設計にしている)。
 */
public class ActualUsageInput {

    private Long materialLotId;
    private BigDecimal usedQty;

    public ActualUsageInput() {
    }

    public Long getMaterialLotId() {
        return materialLotId;
    }

    public void setMaterialLotId(Long materialLotId) {
        this.materialLotId = materialLotId;
    }

    public BigDecimal getUsedQty() {
        return usedQty;
    }

    public void setUsedQty(BigDecimal usedQty) {
        this.usedQty = usedQty;
    }
}
