package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.MaterialPackageSpec;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MaterialPackageSpecMapper {

    int insert(MaterialPackageSpec spec);

    Optional<MaterialPackageSpec> findById(@Param("specId") Long specId);

    /**
     * 特定の材料(materialId)に紐づく梱包仕様を全件取得する。
     * 1つの材料に対して産地違いで複数の梱包仕様が存在しうるため(例: 米粉の愛知産/新潟産)、
     * 戻り値は単一のオブジェクトではなくList(複数件)になる。
     */
    List<MaterialPackageSpec> findByMaterialId(@Param("materialId") Long materialId);

    int update(MaterialPackageSpec spec);

    int deleteById(@Param("specId") Long specId);
}
