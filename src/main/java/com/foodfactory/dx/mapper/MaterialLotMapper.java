package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.MaterialLot;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MaterialLotMapper {

    int insert(MaterialLot lot);

    Optional<MaterialLot> findById(@Param("lotId") Long lotId);

    /**
     * 特定の材料(materialId)のロットを、賞味期限が近い順(FEFO)に並べて全件取得する。
     * フェーズ2で実装する「製造時の材料自動選定」で、この並び順のまま
     * 上から順にロットを引き当てていく処理を書く想定(産地フィルターはService層で行う)。
     */
    List<MaterialLot> findByMaterialIdOrderByExpiry(@Param("materialId") Long materialId);

    /**
     * ロットの残量を、計算(増減)ではなく指定した値に直接上書きする。
     * StockAdjustmentService(棚卸し等による手動補正)専用のメソッドであり、
     * 通常の製造消費・入荷登録の経路からは呼ばない
     * (在庫が理由なく増減しないよう、必ずstock_adjustmentへの記録とセットで使うこと)。
     */
    int setRemainingQty(@Param("lotId") Long lotId, @Param("remainingQty") BigDecimal remainingQty);

    /**
     * 特定の入荷明細(arrivalLineId)から生成されたロットを取得する。
     * 【修正履歴】以前は1明細につきロットは1件まで(UNIQUE制約)という前提だったが、
     * 「結局受け入れる(ACCEPTED_LATE)」対応により、同じ明細から後になって
     * 追加のロットが生成されるケースが生まれたため、この前提は崩れている。
     * 現在この用途では使用しておらず、他の目的で使う場合は複数件返りうる点に注意すること。
     */
    Optional<MaterialLot> findByArrivalLineId(@Param("arrivalLineId") Long arrivalLineId);

    /**
     * 指定した発注(orderId)について、「結局受け入れ」(ACCEPTED_LATE)経由で
     * 生成されたロットの、受け入れた時点の量の合計を返す。
     * 発注の充足率計算で、通常の合格分(material_arrival_line.accepted_qty)に
     * 加算するために使う(要件定義書8.17節を参照)。
     */
    BigDecimal sumAcceptedLateQtyByOrderId(@Param("orderId") Long orderId);

    /**
     * ロットの残量を「指定した量だけ増やす」。decrementRemainingQtyの逆方向の操作。
     * 呼び出し前に必ず stock_adjustment へ調整前後の値を記録してから呼ぶ運用にする
     * (在庫が理由なく増減した記録が残らない、という事態を避けるため)。
     */
    int incrementRemainingQty(@Param("lotId") Long lotId, @Param("addQty") BigDecimal addQty);

    /**
     * ロットの残量を「指定した量だけ減らす」。読み取った値をJava側で引き算するのではなく、
     * DB側で「remaining_qty - usedQty」を1回のSQLで直接計算させる方式にしている。
     *
     * WHERE句に remaining_qty >= usedQty という条件を付けているため、
     * 万が一2つの処理が同時に同じロットを消費しようとしても、
     * 片方が更新した直後は remaining_qty が減っているため、
     * もう片方の条件チェックがDB側で正しく失敗し、更新件数0件(2重消費)を防げる。
     *
     * 戻り値(更新件数)が0の場合、呼び出し側は「在庫が足りなかった、
     * または他の処理と競合した」と判断してエラーにする必要がある。
     */
    int decrementRemainingQty(@Param("lotId") Long lotId, @Param("usedQty") BigDecimal usedQty);
}
