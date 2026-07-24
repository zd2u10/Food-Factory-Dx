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
}
