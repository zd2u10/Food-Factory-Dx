package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.MaterialPackageSpec;
import com.foodfactory.dx.service.MaterialPackageSpecService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 材料ごとの産地(梱包仕様)を管理するController。
 * 1つの材料(米粉など)に対して、複数の産地を登録できるようにするためのAPI。
 * レシピ画面の産地セレクトは、ここで登録された産地一覧を選択肢として使う想定。
 */
@RestController
@RequestMapping("/api/materials/{materialId}/package-specs")
public class MaterialPackageSpecController {

    private final MaterialPackageSpecService materialPackageSpecService;

    public MaterialPackageSpecController(MaterialPackageSpecService materialPackageSpecService) {
        this.materialPackageSpecService = materialPackageSpecService;
    }

    @GetMapping
    public List<MaterialPackageSpec> list(@PathVariable Long materialId) {
        return materialPackageSpecService.listByMaterialId(materialId);
    }

    @PostMapping
    public ResponseEntity<MaterialPackageSpec> create(
            @PathVariable Long materialId,
            @RequestBody MaterialPackageSpec spec) {
        spec.setMaterialId(materialId);
        MaterialPackageSpec created = materialPackageSpecService.create(spec);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{specId}")
    public ResponseEntity<MaterialPackageSpec> update(
            @PathVariable Long materialId,
            @PathVariable Long specId,
            @RequestBody MaterialPackageSpec spec) {
        spec.setMaterialId(materialId);
        MaterialPackageSpec updated = materialPackageSpecService.update(specId, spec);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{specId}")
    public ResponseEntity<Void> delete(@PathVariable Long materialId, @PathVariable Long specId) {
        materialPackageSpecService.delete(specId);
        return ResponseEntity.noContent().build();
    }
}
