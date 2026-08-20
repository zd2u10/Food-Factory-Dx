package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.MaterialOrder;
import com.foodfactory.dx.dto.MaterialOrderSummary;
import com.foodfactory.dx.mapper.HoldResolutionMapper;
import com.foodfactory.dx.mapper.MaterialArrivalLineMapper;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import com.foodfactory.dx.mapper.MaterialOrderMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MaterialOrderService {

    private final MaterialOrderMapper materialOrderMapper;
    private final MaterialArrivalLineMapper materialArrivalLineMapper;
    private final MaterialLotMapper materialLotMapper;
    private final HoldResolutionMapper holdResolutionMapper;

    public MaterialOrderService(MaterialOrderMapper materialOrderMapper,
                                 MaterialArrivalLineMapper materialArrivalLineMapper,
                                 MaterialLotMapper materialLotMapper,
                                 HoldResolutionMapper holdResolutionMapper) {
        this.materialOrderMapper = materialOrderMapper;
        this.materialArrivalLineMapper = materialArrivalLineMapper;
        this.materialLotMapper = materialLotMapper;
        this.holdResolutionMapper = holdResolutionMapper;
    }

    public MaterialOrder createOrder(MaterialOrder order) {
        materialOrderMapper.insert(order);
        return order;
    }

    /**
     * 発注一覧を、画面表示用のサマリ(充足数量・保留履歴の有無を含む)として取得する。
     *
     * 【設計方針】充足数量(totalAcceptedQty)の計算は、ここ(一覧取得)と
     * ProcurementService.recalculateOrderStatus(ステータス自動更新)の両方で
     * 同じ考え方(通常の合格分 + 結局受け入れ分)を使う必要があるが、
     * 計算結果を「状態(ステータス)」として保存する処理と、「画面に表示する数値」を
     * 都度計算する処理は目的が異なるため、あえて重複させている
     * (両方を1箇所に共通化すると、ステータス確定のタイミングと画面表示のタイミングが
     * 密結合になってしまうため)。
     */
    public List<MaterialOrderSummary> listOrders() {
        List<MaterialOrder> orders = materialOrderMapper.findAll();
        return orders.stream()
                .map(order -> {
                    BigDecimal totalAccepted = materialArrivalLineMapper.sumAcceptedQtyByOrderId(order.getOrderId())
                            .add(materialLotMapper.sumAcceptedLateQtyByOrderId(order.getOrderId()));
                    boolean hasHoldHistory = !holdResolutionMapper.findByOrderId(order.getOrderId()).isEmpty();
                    return MaterialOrderSummary.from(order, totalAccepted, hasHoldHistory);
                })
                .collect(Collectors.toList());
    }
}
