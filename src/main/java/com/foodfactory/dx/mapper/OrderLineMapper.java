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

    /**
     * 受注明細を更新する(商品・数量とも変更可能)。
     * 「出荷済み数量を下回る変更は不可」というルールは、Service層で
     * (ShipmentLineMapper.sumShippedQtyByOrderLineIdを使って)事前にチェックしてから呼ぶ。
     */
    int update(OrderLine line);

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

    /**
     * 指定した商品(itemId)について、CANCELLED以外の受注の明細を、
     * 受注日が古い順(先に受けた注文を優先)に取得する。
     * MRPが生成するバッチに、どの受注をどれだけ按分するかを決めるために使う
     * (先に受けた注文から順に、生成されるバッチへ割り当てていく)。
     */
    List<OrderLine> findActiveLinesByItemIdOrderByOrderDate(@Param("itemId") Long itemId);
}
