package com.foodfactory.dx.dto;

import java.util.ArrayList;
import java.util.List;

/** 受注明細1件分の、出荷FEFO選定結果をまとめたもの。 */
public class ShipmentAllocationResult {

    private final List<ShipmentAllocationLine> lines = new ArrayList<>();

    // 残存期限ルール(customer.requiredResidualRatio)を満たすロットだけでは
    // 必要量に届かない場合にtrueになる。
    private boolean shortage = false;

    public List<ShipmentAllocationLine> getLines() {
        return lines;
    }

    public boolean isShortage() {
        return shortage;
    }

    public void setShortage(boolean shortage) {
        this.shortage = shortage;
    }
}
