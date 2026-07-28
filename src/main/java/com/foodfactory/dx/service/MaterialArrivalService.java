package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.MaterialArrival;
import com.foodfactory.dx.mapper.MaterialArrivalMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 入荷ヘッダー(伝票)専用のService。
 *
 * ヘッダーは「いつ・どの仕入先から届いたか」という配送イベントの情報だけを持つ
 * (material_id/order_idは、1回の配送に複数の材料・複数の発注が混在し得るため、
 *  明細側で管理する。詳細は ProcurementService を参照)。
 */
@Service
public class MaterialArrivalService {

    private final MaterialArrivalMapper materialArrivalMapper;

    public MaterialArrivalService(MaterialArrivalMapper materialArrivalMapper) {
        this.materialArrivalMapper = materialArrivalMapper;
    }

    /** 入荷ヘッダー(伝票)を1件登録する。まだ明細(材料・ロット情報)はこの時点では持たない。 */
    public MaterialArrival createArrival(MaterialArrival arrival) {
        materialArrivalMapper.insert(arrival);
        return arrival;
    }

    public List<MaterialArrival> listAll() {
        return materialArrivalMapper.findAll();
    }
}
