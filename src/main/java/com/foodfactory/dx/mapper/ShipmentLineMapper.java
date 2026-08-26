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

    /**
     * 指定した出荷ヘッダー(shipmentId)に紐づく、受注ID(order_line.order_id)を
     * 重複なく取得する。出荷一覧に「どの受注の出荷か」を表示するために使う
     * (要件定義書8.24節を参照。1つの出荷が複数の受注にまたがることは、
     * 現在の運用上は想定していないが、データ構造上は複数返る可能性を考慮し、
     * Listで返す)。
     */
    List<Long> findOrderIdsByShipmentId(@Param("shipmentId") Long shipmentId);

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
