package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.ManufacturingBatch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ManufacturingBatchMapper {

    int insert(ManufacturingBatch batch);

    Optional<ManufacturingBatch> findById(@Param("batchId") Long batchId);

    List<ManufacturingBatch> findAll();

    /**
     * 指定した商品・製造日について、これまでに登録されたバッチの最大連番(batchSeq)を取得する。
     * まだ1件もない場合はnullが返るため、呼び出し側(Service層)で
     * 「nullなら1、そうでなければ+1」という判定を行う。
     */
    Integer findMaxBatchSeq(@Param("itemId") Long itemId, @Param("batchDate") LocalDate batchDate);

    int updateStatus(@Param("batchId") Long batchId, @Param("status") ManufacturingBatch.Status status);

    /** 実際に加えた水の実測量(ml)を記録する(製造実行時に呼ぶ)。 */
    int updateActualHydrationQty(@Param("batchId") Long batchId, @Param("actualHydrationQty") java.math.BigDecimal actualHydrationQty);

    /**
     * 検品完了時の結果をまとめて反映し、ステータスをCOMPLETEDにする専用メソッド。
     * remainingQtyはacceptedQtyと同値で初期化する(フェーズ5で使用)。
     * exceededPlanは、produced_qty(合格+不良)がplanned_qtyを超えていた場合にtrueにする
     * (超過自体はエラーにせず許容するが、後から集計・分析できるよう記録だけ残す)。
     */
    int completeBatch(@Param("batchId") Long batchId,
                       @Param("producedQty") BigDecimal producedQty,
                       @Param("acceptedQty") BigDecimal acceptedQty,
                       @Param("lossQty") BigDecimal lossQty,
                       @Param("lossComment") String lossComment,
                       @Param("exceededPlan") boolean exceededPlan);

    /** 重大な異常によりバッチ全体を破棄する場合の専用メソッド。ステータスをREJECTEDにする。 */
    int rejectBatch(@Param("batchId") Long batchId, @Param("rejectComment") String rejectComment);

    /**
     * 指定した商品の、出荷可能なバッチ(COMPLETED、remainingQty > 0)を、
     * 製造日が古い順に取得する。
     *
     * 商品の賞味期限は「製造日(batchDate) + items.shelfLifeDays」で決まり、
     * shelfLifeDaysは商品ごとに固定値のため、製造日が古い順に並べることが
     * そのまま「期限が近い順(FEFO)」の並び順と一致する
     * (材料側のfindByMaterialIdOrderByExpiryと同じ考え方)。
     */
    List<ManufacturingBatch> findShippableByItemIdOrderByBatchDate(@Param("itemId") Long itemId);

    /**
     * バッチの残量を「指定した量だけ減らす」。material_lotのdecrementRemainingQtyと同じ考え方で、
     * DB側の条件付き更新(remaining_qty >= shippedQty)により、同時出荷時の二重出荷を防ぐ。
     * 更新件数が0件の場合、呼び出し側は在庫不足または競合と判断してエラーにする。
     */
    int decrementRemainingQty(@Param("batchId") Long batchId, @Param("shippedQty") BigDecimal shippedQty);

    /** 製造開始前の取り消し専用メソッド。ステータスをCANCELLEDにする。 */
    int cancelBatch(@Param("batchId") Long batchId, @Param("cancelComment") String cancelComment);

    /**
     * 指定した商品の、完成済み(COMPLETED)バッチの残量合計を取得する(MRPの「有効在庫」に相当)。
     * 該当バッチが1件も無い場合はSUM()がNULLを返すため、SQL側でCOALESCEにより0に変換している。
     */
    BigDecimal sumRemainingQtyByItemId(@Param("itemId") Long itemId);

    /**
     * 指定した商品の、DRAFT/PLAN/MANUFACTURING状態のバッチのplannedQty合計を取得する
     * (MRPの「供給予定量」に相当)。CANCELLED/REJECTEDは対象外のため、
     * 取り消されたバッチが供給予定として残り続けることはない。
     */
    BigDecimal sumPlannedQtyByItemId(@Param("itemId") Long itemId);

    /**
     * 未配置プールのDraftを、特定の日付に配置する。
     * batch_dateとbatch_seqを、この呼び出しの時点で初めてセットする
     * (要件定義書8.19節: デイリー画面で人がバッジをタップ/ドラッグして配置する操作に対応)。
     */
    int assignToDate(@Param("batchId") Long batchId, @Param("batchDate") LocalDate batchDate,
                      @Param("batchSeq") int batchSeq);

    /** 配置済みのDraftを、未配置プールに戻す(batch_date/batch_seqをNULLに戻す)。 */
    int unassignFromDate(@Param("batchId") Long batchId);

    /**
     * status=DRAFTのまま、created_atからdays日以上経過しているバッチを取得する。
     * 運用者が「対応漏れのDraft」に気づけるようにするための一覧取得用。
     */
    List<ManufacturingBatch> findStaleDrafts(@Param("days") int days);
}
