package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.MaterialArrival;
import com.foodfactory.dx.domain.MaterialOrder;
import com.foodfactory.dx.mapper.MaterialArrivalMapper;
import com.foodfactory.dx.mapper.MaterialOrderMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MaterialArrivalService {

    private final MaterialArrivalMapper materialArrivalMapper;
    private final MaterialOrderMapper materialOrderMapper;

    public MaterialArrivalService(MaterialArrivalMapper materialArrivalMapper,
                                   MaterialOrderMapper materialOrderMapper) {
        this.materialArrivalMapper = materialArrivalMapper;
        this.materialOrderMapper = materialOrderMapper;
    }

    /**
     * 入荷ヘッダー(伝票)を1件登録する。まだ明細(ロット情報)はこの時点では持たない。
     *
     * orderIdが指定されている場合、その発注が「どの材料の発注か」を確認し、
     * 入荷側のmaterialIdとして自動的にコピーする。
     * これにより、呼び出し側(画面やAPIの利用者)が発注と矛盾する材料IDを
     * 誤って入力してしまうミスを防いでいる
     * (例: 米粉の発注に対して、誤って玄米粉のmaterialIdを入荷登録してしまう、といった事故)。
     *
     * orderIdがnull(緊急入荷)の場合は、呼び出し側が指定したmaterialIdをそのまま使う。
     */
    public MaterialArrival createArrival(MaterialArrival arrival) {
        if (arrival.getOrderId() != null) {
            MaterialOrder order = materialOrderMapper.findById(arrival.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "指定された発注が見つかりません: orderId=" + arrival.getOrderId()));
            arrival.setMaterialId(order.getMaterialId());
        }
        materialArrivalMapper.insert(arrival);
        return arrival;
    }

    public List<MaterialArrival> listByOrderId(Long orderId) {
        return materialArrivalMapper.findByOrderId(orderId);
    }

    public List<MaterialArrival> listAll() {
        return materialArrivalMapper.findAll();
    }
}
