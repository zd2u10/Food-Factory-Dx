package com.foodfactory.dx.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * バッチ1件分の、全材料に対するFEFO選定結果をまとめたもの。
 * 「複数ロット混合が発生しているか」「不足している材料があるか」を
 * 画面側(将来のReact実装)が一目で判定できるよう、判定用のフラグも持たせている。
 */
public class FefoAllocationResult {

    private final List<FefoAllocationLine> lines = new ArrayList<>();

    // どれか1つでも材料が不足している場合にtrueになる。
    // 産地制約は現場判断での代替を許可しないため、
    // trueの場合は製造実行そのものをブロックする(要件定義書 5.1節の方針)。
    private boolean shortage = false;

    // shortageがtrueの場合、どの材料が不足しているかを人間が読める形で記録する。
    private final List<String> shortageMaterialNames = new ArrayList<>();

    public List<FefoAllocationLine> getLines() {
        return lines;
    }

    public boolean isShortage() {
        return shortage;
    }

    public void setShortage(boolean shortage) {
        this.shortage = shortage;
    }

    public List<String> getShortageMaterialNames() {
        return shortageMaterialNames;
    }

    /**
     * 同じ材料に対して複数のロットが割り当てられているかどうかを判定する。
     * これが1つでも見つかった場合、フェーズ0で決めた通り
     * 「バッチ全体で1回だけ確認モーダルを表示する」トリガーとして使う想定。
     */
    public boolean hasMixedLots() {
        return lines.stream()
                .collect(java.util.stream.Collectors.groupingBy(FefoAllocationLine::getMaterialId))
                .values().stream()
                .anyMatch(group -> group.size() > 1);
    }
}
