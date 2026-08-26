package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.BatchMaterialUsage;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BatchMaterialUsageMapper {

    int insert(BatchMaterialUsage usage);

    /** 特定のバッチ(batchId)で使用された材料の記録を全件取得する。 */
    List<BatchMaterialUsage> findByBatchId(@Param("batchId") Long batchId);

    /**
     * 指定したバッチが、「結局受け入れ」(ACCEPTED_LATE)経由のロットを
     * 1つでも使用していれば true を返す。material_lot.origin_hold_id が
     * nullでないロットを、そのバッチの使用実績から辿って判定する
     * (要件定義書8.23節: FEFO画面・実行済み一覧の両方で、保留対応を経た
     * 材料であることが分かるようにする)。
     */
    boolean usedHeldLot(@Param("batchId") Long batchId);

    /**
     * 指定したバッチが、一度でも「要確認」→検査結果登録(生存量として復帰)を経た
     * ロット(material_lot.was_reviewed = true)を使用したかを判定する。
     * usedHeldLot(結局受け入れ経由)とはリスクの原因が異なるため、別々に判定する
     * (要件定義書8.23節を参照)。
     */
    boolean usedReviewedLot(@Param("batchId") Long batchId);
}
