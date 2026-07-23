package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.RecipeItem;
import com.foodfactory.dx.mapper.RecipeItemMapper;
import java.util.List;
import org.springframework.stereotype.Service;

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

    public List<RecipeItem> listByItemId(Long itemId) {
        return recipeItemMapper.findByItemId(itemId);
    }
}
