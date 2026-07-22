package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.RecipeItem;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecipeItemMapper {

    int insert(RecipeItem recipeItem);

    Optional<RecipeItem> findById(@Param("recipeItemId") Long recipeItemId);

    /**
     * 特定の商品(itemId)のレシピ明細を全件取得する。
     * 「商品カードをタップしてレシピを展開する」機能は、このメソッドの結果を使って実装する想定。
     */
    List<RecipeItem> findByItemId(@Param("itemId") Long itemId);

    int update(RecipeItem recipeItem);

    int deleteById(@Param("recipeItemId") Long recipeItemId);
}
