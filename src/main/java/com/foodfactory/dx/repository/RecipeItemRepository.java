package com.foodfactory.dx.repository;

import com.foodfactory.dx.entity.RecipeItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeItemRepository extends JpaRepository<RecipeItem, Long> {

    List<RecipeItem> findByItem_ItemId(Long itemId);
}
