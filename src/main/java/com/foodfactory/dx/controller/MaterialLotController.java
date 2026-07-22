package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialLot;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 材料ロットは「入荷検品を登録した結果として自動生成される」ものであり、
 * 人が直接ロットを新規作成するAPIは用意していない
 * (登録経路をProcurementService経由の1つに絞ることで、
 *  在庫データの発生源を追いやすくするため)。
 * そのため、ここでは確認用の一覧取得だけを提供する。
 *
 * まだService層を挟むほどの複雑さがないため、
 * このControllerだけは直接Mapperを呼び出している(小規模な参照専用の処理のため許容している)。
 */
@RestController
@RequestMapping("/api/material-lots")
public class MaterialLotController {

    private final MaterialLotMapper materialLotMapper;

    public MaterialLotController(MaterialLotMapper materialLotMapper) {
        this.materialLotMapper = materialLotMapper;
    }

    /** 指定した材料のロットを、賞味期限が近い順(FEFO順)に取得する。 */
    @GetMapping
    public List<MaterialLot> listByMaterial(@RequestParam Long materialId) {
        return materialLotMapper.findByMaterialIdOrderByExpiry(materialId);
    }
}
