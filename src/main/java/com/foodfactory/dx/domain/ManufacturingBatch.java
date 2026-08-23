package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 製造バッチ(=製造ロット)。
 * 1回の製造実行を1件のレコードとして表す。batchIdが、商品版の「ロットID」に相当する
 * (材料側のmaterial_lot.lotIdと同じ役割)。
 */
public class ManufacturingBatch {

    public enum Status {
        DRAFT,          // 草案(MRP提案または手動追加された直後)
        PLAN,           // 予定確定(人が内容を確認しOKを出した状態)
        MANUFACTURING,  // 製造中(実行ボタンが押され、材料を消費した状態)
        COMPLETED,      // 完了(検品を経て商品在庫に反映された状態)
        REJECTED,       // 破棄(重大な異常によりバッチ全体を無効化した状態)
        CANCELLED       // 取り消し(製造開始前に人が取り消した状態。REJECTEDとは発生タイミングが異なる)
    }

    public enum OriginType {
        MRP_AUTO,  // MRPが自動生成したバッチ(フェーズ4で使用。現時点では使わない)
        MANUAL     // 人が手動で追加したバッチ
    }

    private Long batchId;              // 主キー。商品版の「ロットID」として使う
    private Long itemId;                // どの商品のバッチか
    private Long mrpRunId;              // どのMRP実行から生成されたか(フェーズ4まではnullのまま)
    private LocalDate batchDate;        // 製造日。まだどの日にも配置されていない未配置プールの場合はnull
    private Integer batchSeq;           // その日・その商品の何バッチ目か(1から始まる連番)。batchDateと同様、未配置の間はnull

    // フィールド宣言時点で初期値を持たせている(MaterialOrder.statusと同じ理由)。
    private Status status = Status.DRAFT;             // バッチの進行状態
    private OriginType originType = OriginType.MANUAL; // 自動生成か手動追加か

    private String createdBy;           // 手動追加時の担当者(任意)
    private BigDecimal plannedQty;      // 計画数量。通常はitems.standardBatchQtyと同値
    private BigDecimal producedQty;     // 完了時に確定する製造数(合格+不良の合計)
    private BigDecimal acceptedQty;     // 完了時に確定する合格数(商品在庫に計上される数)
    private BigDecimal remainingQty;    // 出荷等で減っていく残量。完了時にacceptedQtyと同値で初期化(フェーズ5で使用)
    private boolean exceededPlan;       // 完了時、produced_qty(合格+不良)がplannedQtyを超えていた場合true(超過は許容するが記録は残す)
    private BigDecimal lossQty;         // 完了時に確定する軽微な不良数
    private String lossComment;         // 軽微な不良の理由コメント
    private String rejectComment;       // REJECTEDになった場合の理由コメント
    private String cancelComment;       // CANCELLEDになった場合の理由コメント(製造開始前の取り消し)
    private java.math.BigDecimal actualHydrationQty; // 実際に加えた水の実測量(ml)。トレーサビリティ記録用

    private LocalDateTime createdAt;    // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;    // 更新日時(DB側で自動設定)

    public ManufacturingBatch() {
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getMrpRunId() {
        return mrpRunId;
    }

    public void setMrpRunId(Long mrpRunId) {
        this.mrpRunId = mrpRunId;
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDate batchDate) {
        this.batchDate = batchDate;
    }

    public Integer getBatchSeq() {
        return batchSeq;
    }

    public void setBatchSeq(Integer batchSeq) {
        this.batchSeq = batchSeq;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OriginType getOriginType() {
        return originType;
    }

    public void setOriginType(OriginType originType) {
        this.originType = originType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public BigDecimal getPlannedQty() {
        return plannedQty;
    }

    public void setPlannedQty(BigDecimal plannedQty) {
        this.plannedQty = plannedQty;
    }

    public BigDecimal getProducedQty() {
        return producedQty;
    }

    public void setProducedQty(BigDecimal producedQty) {
        this.producedQty = producedQty;
    }

    public BigDecimal getAcceptedQty() {
        return acceptedQty;
    }

    public void setAcceptedQty(BigDecimal acceptedQty) {
        this.acceptedQty = acceptedQty;
    }

    public BigDecimal getRemainingQty() {
        return remainingQty;
    }

    public void setRemainingQty(BigDecimal remainingQty) {
        this.remainingQty = remainingQty;
    }

    public boolean isExceededPlan() {
        return exceededPlan;
    }

    public void setExceededPlan(boolean exceededPlan) {
        this.exceededPlan = exceededPlan;
    }

    public BigDecimal getLossQty() {
        return lossQty;
    }

    public void setLossQty(BigDecimal lossQty) {
        this.lossQty = lossQty;
    }

    public String getLossComment() {
        return lossComment;
    }

    public void setLossComment(String lossComment) {
        this.lossComment = lossComment;
    }

    public String getRejectComment() {
        return rejectComment;
    }

    public void setRejectComment(String rejectComment) {
        this.rejectComment = rejectComment;
    }

    public String getCancelComment() {
        return cancelComment;
    }

    public void setCancelComment(String cancelComment) {
        this.cancelComment = cancelComment;
    }

    public java.math.BigDecimal getActualHydrationQty() {
        return actualHydrationQty;
    }

    public void setActualHydrationQty(java.math.BigDecimal actualHydrationQty) {
        this.actualHydrationQty = actualHydrationQty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
