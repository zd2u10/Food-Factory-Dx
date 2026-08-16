package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.MaterialPackageSpec;
import com.foodfactory.dx.mapper.MaterialPackageSpecMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MaterialPackageSpecService {

    private final MaterialPackageSpecMapper materialPackageSpecMapper;

    public MaterialPackageSpecService(MaterialPackageSpecMapper materialPackageSpecMapper) {
        this.materialPackageSpecMapper = materialPackageSpecMapper;
    }

    public List<MaterialPackageSpec> listByMaterialId(Long materialId) {
        return materialPackageSpecMapper.findByMaterialId(materialId);
    }

    public MaterialPackageSpec create(MaterialPackageSpec spec) {
        materialPackageSpecMapper.insert(spec);
        return spec;
    }

    public MaterialPackageSpec update(Long specId, MaterialPackageSpec spec) {
        materialPackageSpecMapper.findById(specId)
                .orElseThrow(() -> new IllegalArgumentException("指定された梱包仕様が見つかりません: specId=" + specId));
        spec.setSpecId(specId);
        materialPackageSpecMapper.update(spec);
        return spec;
    }

    /**
     * 産地(梱包仕様)を削除する。
     * 物理削除だが、material_package_specは「材料の付随マスタ情報」であり、
     * 過去の入荷記録は material_arrival_line 側に産地情報のコピーを持っているため、
     * ここを消しても過去の記録が壊れることはない(material・itemsの論理削除とは事情が異なる)。
     */
    public void delete(Long specId) {
        materialPackageSpecMapper.findById(specId)
                .orElseThrow(() -> new IllegalArgumentException("指定された梱包仕様が見つかりません: specId=" + specId));
        materialPackageSpecMapper.deleteById(specId);
    }
}
