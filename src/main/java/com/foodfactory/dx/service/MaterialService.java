package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Material;
import com.foodfactory.dx.mapper.MaterialMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MaterialService {

    private final MaterialMapper materialMapper;

    public MaterialService(MaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    /**
     * 材料を分類(category)・有効フラグ(active)で絞り込んで取得する。
     * どちらもnullを渡せば「絞り込まない(全件対象)」という意味になる
     * (MaterialMapper.findByFiltersの動的SQLがその条件をよしなに扱ってくれる)。
     */
    public List<Material> listMaterials(Material.Category category, Boolean active) {
        return materialMapper.findByFilters(category, active);
    }

    /**
     * 新しい材料を1件登録する。
     * activeはdomainクラス側でフィールド初期値としてtrueを持たせているため、
     * 呼び出し側が明示的に指定しなくても「有効」な状態で登録される。
     */
    public Material createMaterial(Material material) {
        materialMapper.insert(material);
        return material;
    }

    /**
     * 材料の内容を編集する。
     * 渡された material の materialId を使って更新対象を特定する。
     * 対象が存在しない場合はエラーにする(存在しないIDへの更新を静かに無視しないため)。
     */
    public Material updateMaterial(Long materialId, Material material) {
        materialMapper.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("指定された材料が見つかりません: materialId=" + materialId));
        material.setMaterialId(materialId);
        materialMapper.update(material);
        return material;
    }

    /**
     * 材料を廃版(論理削除)にする。物理的にデータを消すのではなく、
     * is_active を false にするだけ。過去のレシピ・入荷記録等から参照されている場合でも、
     * それらの記録を壊さずに「もう新規では使わない」ことを表現できる。
     */
    public void deactivateMaterial(Long materialId) {
        materialMapper.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("指定された材料が見つかりません: materialId=" + materialId));
        materialMapper.setActive(materialId, false);
    }

    /** 廃版にした材料を、再び有効な状態に戻す(復元)。 */
    public void reactivateMaterial(Long materialId) {
        materialMapper.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("指定された材料が見つかりません: materialId=" + materialId));
        materialMapper.setActive(materialId, true);
    }
}
