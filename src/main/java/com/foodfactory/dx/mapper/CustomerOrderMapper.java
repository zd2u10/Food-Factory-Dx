package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.CustomerOrder;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerOrderMapper {

    int insert(CustomerOrder order);

    Optional<CustomerOrder> findById(@Param("orderId") Long orderId);

    List<CustomerOrder> findAll();

    /**
     * ステータスだけを更新する専用メソッド。
     * 出荷登録のたびにShipmentServiceが充足状況を再集計して呼ぶ想定
     * (material_orderのstatus更新と同じ考え方)。
     */
    int updateStatus(@Param("orderId") Long orderId, @Param("status") CustomerOrder.Status status);
}
