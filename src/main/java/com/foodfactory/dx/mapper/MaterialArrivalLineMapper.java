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

    /**
     * 特定の発注(orderId)に対して、これまでに検品合格した数量の合計を取得する。
     *
     * 発注(material_order) 1件に対して、入荷ヘッダー(material_arrival)が複数(分納)、
     * さらにその入荷ヘッダーの下に明細(material_arrival_line)が複数、という3階層構造のため、
     * 「発注が今どれだけ充足されているか」を知るには
     * material_arrival_line → material_arrival → material_order と2段階JOINして
     * accepted_qty を合計する必要がある。
     *
     * 戻り値がBigDecimalなのは、該当する明細が1件もない場合にSQLのSUM()がNULLを返すため、
     * 呼び出し側でnullチェックが必要になることを型で示すため
     * (int等のプリミティブ型では表現できない)。
     */
    BigDecimal sumAcceptedQtyByOrderId(@Param("orderId") Long orderId);

    /**
     * 検品結果(合格数量・保留数量・チェック項目)を登録済みの明細に反映する更新。
     * 明細の基本情報(産地・期限・ロット番号等)は変わらない前提のため、
     * 検品に関わる列だけを更新対象にしている。
     */
    int updateInspectionResult(MaterialArrivalLine line);
}
