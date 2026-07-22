package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.MaterialOrder;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MaterialOrderMapper {

    int insert(MaterialOrder order);

    Optional<MaterialOrder> findById(@Param("orderId") Long orderId);

    List<MaterialOrder> findAll();

    /**
     * 発注のステータスだけを更新する専用メソッド。
     *
     * updateではなくupdateStatusという専用名にしているのは、
     * 「発注数量や発注日など、人が入力した値を書き換える更新」と
     * 「入荷状況の変化に応じてシステムが自動的にステータスだけ書き換える更新」を
     * コードの見た目からもはっきり区別するため。
     * (Service層で「なぜこの更新が起きたのか」を追いやすくする狙い)
     */
    int updateStatus(@Param("orderId") Long orderId, @Param("status") MaterialOrder.Status status);
}
