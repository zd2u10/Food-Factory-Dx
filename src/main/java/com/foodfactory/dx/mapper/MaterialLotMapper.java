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
     * ロットの残量を更新する。
     * 呼び出す際は、Service層側で「現在の残量 - 使用量」を計算した結果を渡す想定
     * (このメソッド自身は引き算をしない。単に指定された値で上書きするだけ)。
     */
    int updateRemainingQty(@Param("lotId") Long lotId, @Param("remainingQty") BigDecimal remainingQty);
}
