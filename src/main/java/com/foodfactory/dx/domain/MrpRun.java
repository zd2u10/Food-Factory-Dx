package com.foodfactory.dx.domain;

import java.time.LocalDateTime;

/** MRP実行履歴。 */
public class MrpRun {

    public enum TriggeredBy {
        AUTO,   // 1日1回の定期実行
        MANUAL, // 人による手動実行
        EVENT   // CANCELLED/REJECTED発生時の即時再計算
    }

    private Long runId;              // 主キー
    private LocalDateTime runAt;     // 実行日時(DB側で自動設定)
    private TriggeredBy triggeredBy; // 実行のきっかけ
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定)

    public MrpRun() {
    }

    public MrpRun(TriggeredBy triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public LocalDateTime getRunAt() {
        return runAt;
    }

    public void setRunAt(LocalDateTime runAt) {
        this.runAt = runAt;
    }

    public TriggeredBy getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(TriggeredBy triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
