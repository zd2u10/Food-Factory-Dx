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

    /** 全件取得する(ステータス問わず)。監査・トレーサビリティ確認用に、対応済みも含めて表示する際に使う。 */
    List<HoldResolution> findAll();

    /**
     * 指定した発注(material_order)に関わった保留の履歴を、ステータス問わず全件取得する。
     * hold_resolution は order_id を直接持たないため、
     * material_arrival_line を経由して order_id を絞り込む(JOIN)。
     * 発注詳細画面で「この発注では、過去にどんな保留・対応があったか」を表示するために使う。
     */
    List<HoldResolution> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 対応方針を確定し、ステータスをRESOLVEDにする更新。
     * resolvedLineId は EXCHANGED の場合のみ値が入り、それ以外はnullのまま。
     */
    int resolve(@Param("holdId") Long holdId,
                @Param("resolutionType") HoldResolution.ResolutionType resolutionType,
                @Param("resolvedLineId") Long resolvedLineId,
                @Param("comment") String comment);
}
