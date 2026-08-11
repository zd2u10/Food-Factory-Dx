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

    /**
     * 複数のレシピ明細を一括登録する。
     *
     * @Transactional: リスト内の1件でも登録に失敗した場合、それより前に登録済みだった分も
     *   全て取り消す(ロールバックする)。「主原料は登録できたが、液体材料の登録だけ失敗し、
     *   加水率計算に必要な材料が欠けたレシピが中途半端に保存されてしまう」という事態を防ぐため。
     */
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

    /**
     * レシピ明細を編集する。materialId(材料そのもの)の変更も含めて、全項目を上書きできる。
     * 材料の廃版に伴う入れ替わりなど、レシピの材料構成自体が変わる運用を想定している。
     */
    public RecipeItem updateRecipeItem(Long recipeItemId, RecipeItem recipeItem) {
        recipeItemMapper.findById(recipeItemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定されたレシピ明細が見つかりません: recipeItemId=" + recipeItemId));
        recipeItem.setRecipeItemId(recipeItemId);
        recipeItemMapper.update(recipeItem);
        return recipeItem;
    }
}
