package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.OrderLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderLineMapper {

    int insert(OrderLine line);

    Optional<OrderLine> findById(@Param("lineId") Long lineId);

    /** 特定の受注(orderId)の明細を全件取得する。 */
    List<OrderLine> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 特定の受注(orderId)の、明細の注文数量の合計を取得する。
     * 出荷状況(PARTIALLY_SHIPPED/COMPLETED)を判定する際、
     * 「発注全体でいくつ注文されたか」の基準値として使う。
     */
    BigDecimal sumQtyByOrderId(@Param("orderId") Long orderId);

    /**
     * 指定した商品(itemId)について、CANCELLED以外の受注に含まれる注文数量の合計を取得する
     * (MRPの受注残計算の「注文された総数」の部分に使う。実際の受注残は、
     * この値からShipmentLineMapper.sumShippedQtyByItemIdを差し引いて算出する)。
     */
    BigDecimal sumActiveQtyByItemId(@Param("itemId") Long itemId);
}
