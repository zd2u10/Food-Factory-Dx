package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.StockAdjustment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockAdjustmentMapper {

    int insert(StockAdjustment adjustment);

    /** 特定のロット(lotId)に対する調整履歴を全件取得する。 */
    List<StockAdjustment> findByLotId(@Param("lotId") Long lotId);
}
