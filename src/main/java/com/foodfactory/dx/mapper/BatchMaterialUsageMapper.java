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
}
