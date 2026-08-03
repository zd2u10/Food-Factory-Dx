package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.ShipmentLine;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShipmentLineMapper {

    int insert(ShipmentLine line);

    /** 特定の出荷ヘッダー(shipmentId)に属する明細を全件取得する。 */
    List<ShipmentLine> findByShipmentId(@Param("shipmentId") Long shipmentId);

    /** 特定の受注明細(orderLineId)に対する出荷実績を全件取得する。 */
    List<ShipmentLine> findByOrderLineId(@Param("orderLineId") Long orderLineId);

    /** 特定の受注明細(orderLineId)について、これまでに出荷された数量の合計を取得する。 */
    BigDecimal sumShippedQtyByOrderLineId(@Param("orderLineId") Long orderLineId);

    /**
     * 特定の受注(orderId)について、これまでに出荷された数量の合計を取得する。
     * order_line経由でJOINし、受注全体の充足状況(PARTIALLY_SHIPPED/COMPLETED)を判定するために使う。
     */
    BigDecimal sumShippedQtyByOrderId(@Param("orderId") Long orderId);

    /**
     * 指定した商品(itemId)について、これまでに出荷された数量の合計を取得する
     * (MRPの受注残計算で、注文された総数から差し引く「既に出荷済みの数」として使う)。
     */
    BigDecimal sumShippedQtyByItemId(@Param("itemId") Long itemId);
}
