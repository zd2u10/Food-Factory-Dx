package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.HoldResolution;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HoldResolutionMapper {

    int insert(HoldResolution hold);

    Optional<HoldResolution> findById(@Param("holdId") Long holdId);

    /** ステータス(ON_HOLD/RESOLVED)で絞り込んで取得する。対応待ち一覧の表示に使う。 */
    List<HoldResolution> findByStatus(@Param("status") HoldResolution.Status status);

    /**
     * 対応方針を確定し、ステータスをRESOLVEDにする更新。
     * resolvedLineId は EXCHANGED の場合のみ値が入り、それ以外はnullのまま。
     */
    int resolve(@Param("holdId") Long holdId,
                @Param("resolutionType") HoldResolution.ResolutionType resolutionType,
                @Param("resolvedLineId") Long resolvedLineId,
                @Param("comment") String comment);
}
