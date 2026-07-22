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

    int update(Item item);

    int deleteById(@Param("itemId") Long itemId);
}
