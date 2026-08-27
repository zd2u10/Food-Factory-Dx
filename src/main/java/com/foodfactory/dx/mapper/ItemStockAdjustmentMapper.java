package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.ItemStockAdjustment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ItemStockAdjustmentMapper {

    int insert(ItemStockAdjustment adjustment);

    List<ItemStockAdjustment> findByBatchId(@Param("batchId") Long batchId);
}
