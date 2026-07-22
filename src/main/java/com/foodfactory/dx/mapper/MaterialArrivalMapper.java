package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.MaterialArrival;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MaterialArrivalMapper {

    int insert(MaterialArrival arrival);

    Optional<MaterialArrival> findById(@Param("arrivalId") Long arrivalId);

    /** 特定の発注(orderId)に紐づく入荷ヘッダーを全件取得する(分納の履歴確認に使う)。 */
    List<MaterialArrival> findByOrderId(@Param("orderId") Long orderId);

    List<MaterialArrival> findAll();
}
