package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保留対応記録。
 * 検品で一部保留(heldQty > 0)が発生した入荷明細に対して、
 * 「返品」「交換」「結局受け入れる」のいずれで決着したかを記録する。
 */
public class HoldResolution {

    public enum ResolutionType {
        RETURNED,       // 返品(自社に問題がない仕入先都合の返却。廃棄=ロスとしては扱わない)
        EXCHANGED,      // 交換(新しい入荷明細が代わりに登録される)
        ACCEPTED_LATE   // 結局受け入れる(元の明細のheldQtyをacceptedQtyに繰り入れる)
    }

    public enum Status {
        ON_HOLD,   // 対応方針が未確定
        RESOLVED   // 対応済み
    }

    private Long holdId;                     // 主キー
    private Long lineId;                     // 保留が発生した元の入荷明細
    private BigDecimal heldQtySnapshot;      // 保留発生時点の保留数量のスナップショット(元明細のheldQtyが後で0に書き換わっても追跡できる)
    private ResolutionType resolutionType;   // 対応方針。未確定の間はnull
    private Long resolvedLineId;             // 交換品として新規登録された入荷明細(EXCHANGEDの場合のみ)
    private Status status = Status.ON_HOLD;  // 保留中か対応済みか
    private String comment;                  // 対応理由・メモ
    private LocalDateTime createdAt;         // 登録日時(=保留が発生した日時、DB側で自動設定)
    private LocalDateTime updatedAt;         // 更新日時(=対応が確定した日時、DB側で自動設定)

    public HoldResolution() {
    }

    public HoldResolution(Long lineId, BigDecimal heldQtySnapshot) {
        this.lineId = lineId;
        this.heldQtySnapshot = heldQtySnapshot;
    }

    public Long getHoldId() {
        return holdId;
    }

    public void setHoldId(Long holdId) {
        this.holdId = holdId;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public BigDecimal getHeldQtySnapshot() {
        return heldQtySnapshot;
    }

    public void setHeldQtySnapshot(BigDecimal heldQtySnapshot) {
        this.heldQtySnapshot = heldQtySnapshot;
    }

    public ResolutionType getResolutionType() {
        return resolutionType;
    }

    public void setResolutionType(ResolutionType resolutionType) {
        this.resolutionType = resolutionType;
    }

    public Long getResolvedLineId() {
        return resolvedLineId;
    }

    public void setResolvedLineId(Long resolvedLineId) {
        this.resolvedLineId = resolvedLineId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
