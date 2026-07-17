package com.foodfactory.dx.repository;

import com.foodfactory.dx.entity.MaterialPackageSpec;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialPackageSpecRepository extends JpaRepository<MaterialPackageSpec, Long> {

    List<MaterialPackageSpec> findByMaterial_MaterialId(Long materialId);
}
