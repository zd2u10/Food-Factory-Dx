package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.BatchOrderAllocation;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BatchOrderAllocationMapper {

    int insert(BatchOrderAllocation allocation);

    /** 指定した受注に関連する、按分の全レコードを取得する(受注キャンセル時の判定に使う)。 */
    List<BatchOrderAllocation> findByOrderId(@Param("orderId") Long orderId);

    /** 指定したバッチに関連する、按分の全レコードを取得する(そのバッチの内訳確認に使う)。 */
    List<BatchOrderAllocation> findByBatchId(@Param("batchId") Long batchId);

    /**
     * 指定したバッチについて、まだ有効な(キャンセルされていない)受注に紐づく按分の合計を返す。
     * MRPの供給予定量計算で、「バッチ全体のplannedQtyではなく、実質的に有効な量」を
     * 求めるために使う(安全在庫由来の部分は、この合計に別途加算する必要がある)。
     */
    BigDecimal sumActiveAllocatedQtyByBatchId(@Param("batchId") Long batchId);
}
