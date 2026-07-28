package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.MaterialArrivalLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MaterialArrivalLineMapper {

    int insert(MaterialArrivalLine line);

    Optional<MaterialArrivalLine> findById(@Param("lineId") Long lineId);

    /** 特定の入荷ヘッダー(arrivalId)に属する明細を全件取得する。 */
    List<MaterialArrivalLine> findByArrivalId(@Param("arrivalId") Long arrivalId);

    /** 特定の発注(orderId)に紐づく明細を全件取得する(orderIdが明細側に移ったため、直接1段階で取得できる)。 */
    List<MaterialArrivalLine> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 特定の発注(orderId)に対して、これまでに検品合格した数量の合計を取得する。
     *
     * orderIdを明細(material_arrival_line)側に直接持たせたことで、
     * 以前必要だった material_arrival 経由のJOINが不要になり、1テーブルへのWHERE集計だけで済む
     * (1回の配送に複数の異なる発注・異なる材料が混在しても、明細ごとに正しく区別できる)。
     *
     * 戻り値がBigDecimalなのは、該当する明細が1件もない場合にSQLのSUM()がNULLを返すため、
     * 呼び出し側でnullチェックが必要になることを型で示すため。
     */
    BigDecimal sumAcceptedQtyByOrderId(@Param("orderId") Long orderId);

    /**
     * 検品結果(合格数量・保留数量・チェック項目)を登録済みの明細に反映する更新。
     * 明細の基本情報(産地・期限・ロット番号等)は変わらない前提のため、
     * 検品に関わる列だけを更新対象にしている。
     */
    int updateInspectionResult(MaterialArrivalLine line);
}
