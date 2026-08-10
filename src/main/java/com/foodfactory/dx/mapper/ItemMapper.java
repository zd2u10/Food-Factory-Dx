package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.Item;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ItemMapper {

    int insert(Item item);

    Optional<Item> findById(@Param("itemId") Long itemId);

    List<Item> findAll();

    /** 有効フラグで絞り込んで取得する。activeにnullを渡せば絞り込まない(全件対象)。 */
    List<Item> findByFilters(@Param("active") Boolean active);

    int update(Item item);

    /** 有効/廃版フラグだけを更新する(論理削除・復元の両方に使う)。物理削除は行わない。 */
    int setActive(@Param("itemId") Long itemId, @Param("active") boolean active);
}
