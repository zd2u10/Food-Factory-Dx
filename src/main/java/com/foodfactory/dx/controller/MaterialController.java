package com.foodfactory.dx.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodfactory.dx.domain.Material;
import com.foodfactory.dx.service.MaterialService;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * 材料の一覧を取得する。分類・有効フラグでの絞り込みに対応する。
     *
     * @RequestParam(required = false): クエリパラメータが無ければnullのまま受け取る、という意味。
     *   例:
     *     GET /api/materials                     → 全件
     *     GET /api/materials?category=RAW        → 原料のみ
     *     GET /api/materials?active=true         → 有効なものだけ
     *     GET /api/materials?category=RAW&active=false → 廃版になった原料だけ
     */
    @GetMapping
    public List<Material> list(
            @RequestParam(required = false) Material.Category category,
            @RequestParam(required = false) Boolean active) {
        return materialService.listMaterials(category, active);
    }

    @PostMapping
    public ResponseEntity<Material> create(@RequestBody Material material) {
        Material created = materialService.createMaterial(material);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 材料の内容を編集する。 */
    @PutMapping("/{materialId}")
    public ResponseEntity<Material> update(
            @PathVariable Long materialId,
            @RequestBody Material material) {
        Material updated = materialService.updateMaterial(materialId, material);
        return ResponseEntity.ok(updated);
    }

    /** 材料を廃版(論理削除)にする。 */
    @PostMapping("/{materialId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long materialId) {
        materialService.deactivateMaterial(materialId);
        return ResponseEntity.ok().build();
    }

    /** 廃版にした材料を有効な状態に戻す。 */
    @PostMapping("/{materialId}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable Long materialId) {
        materialService.reactivateMaterial(materialId);
        return ResponseEntity.ok().build();
    }
}
