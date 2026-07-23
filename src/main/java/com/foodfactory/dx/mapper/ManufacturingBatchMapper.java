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
     * (通常運用の軽微な不良は produced/accepted/loss の内訳で表現し、バッチ自体は完了扱いのまま)
     *
     * remainingQty(出荷等で減っていく残量)は、この完了処理の時点でacceptedQtyと同じ値で
     * 初期化する。フェーズ5(出荷管理)で、出荷のたびにこの値を減らしていく想定。
     */
    int completeBatch(@Param("batchId") Long batchId,
                       @Param("producedQty") BigDecimal producedQty,
                       @Param("acceptedQty") BigDecimal acceptedQty,
                       @Param("lossQty") BigDecimal lossQty,
                       @Param("lossComment") String lossComment);

    /** 重大な異常によりバッチ全体を破棄する場合の専用メソッド。ステータスをREJECTEDにする。 */
    int rejectBatch(@Param("batchId") Long batchId, @Param("rejectComment") String rejectComment);
}
