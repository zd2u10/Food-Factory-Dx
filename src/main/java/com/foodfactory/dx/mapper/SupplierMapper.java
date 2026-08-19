package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.Supplier;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SupplierMapper {

    int insert(Supplier supplier);

    Optional<Supplier> findById(@Param("supplierId") Long supplierId);

    /** 有効フラグで絞り込んで取得する。activeにnullを渡せば絞り込まない(全件対象)。 */
    List<Supplier> findByFilters(@Param("active") Boolean active);

    int update(Supplier supplier);

    /** 有効/廃版フラグだけを更新する(論理削除・復元の両方に使う)。物理削除は行わない。 */
    int setActive(@Param("supplierId") Long supplierId, @Param("active") boolean active);
}
