package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.RecipeItem;
import com.foodfactory.dx.mapper.RecipeItemMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeItemService {

    private final RecipeItemMapper recipeItemMapper;

    public RecipeItemService(RecipeItemMapper recipeItemMapper) {
        this.recipeItemMapper = recipeItemMapper;
    }

    public RecipeItem createRecipeItem(RecipeItem recipeItem) {
        recipeItemMapper.insert(recipeItem);
        return recipeItem;
    }

    @Transactional
    public List<RecipeItem> createRecipeItemsBulk(List<RecipeItem> recipeItems) {
        for (RecipeItem recipeItem : recipeItems) {
            recipeItemMapper.insert(recipeItem);
        }
        return recipeItems;
    }

    public List<RecipeItem> listByItemId(Long itemId) {
        return recipeItemMapper.findByItemId(itemId);
    }

    public RecipeItem updateRecipeItem(Long recipeItemId, RecipeItem recipeItem) {
        recipeItemMapper.findById(recipeItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定されたレシピ明細が見つかりません: recipeItemId=" + recipeItemId));
        recipeItem.setRecipeItemId(recipeItemId);
        recipeItemMapper.update(recipeItem);
        return recipeItem;
    }

    /**
     * レシピ明細を削除する。誤って登録してしまった明細を取り消すための操作。
     * recipe_itemは、他のテーブル(manufacturing_batch等)から直接参照されるものではなく、
     * FEFO計算のたびに都度読み取られるだけの「現在のレシピ内容」という位置づけのため、
     * material・itemsのような論理削除ではなく、物理削除で問題ない。
     */
    public void deleteRecipeItem(Long recipeItemId) {
        recipeItemMapper.findById(recipeItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定されたレシピ明細が見つかりません: recipeItemId=" + recipeItemId));
        recipeItemMapper.deleteById(recipeItemId);
    }
}
