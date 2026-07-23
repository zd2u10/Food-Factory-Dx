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
