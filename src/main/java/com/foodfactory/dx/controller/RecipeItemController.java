package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.RecipeItem;
import com.foodfactory.dx.service.RecipeItemService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * レシピ明細(商品×材料)のController。
 * URLを /api/items/{itemId}/recipe-items としているのは、
 * 「レシピ明細は必ずどこかの商品に属する」という親子関係をURL自体で表現するため
 * (MaterialArrivalLineControllerと同じ考え方)。
 *
 * リクエストボディの allowedOrigins は、"愛知,三重" のようなカンマ区切りの文字列を
 * そのまま送ってもらう想定(domainクラスのgetAllowedOrigins/setAllowedOriginsが
 * このJSONプロパティ名にそのまま対応するため、特別な変換は不要)。
 */
@RestController
@RequestMapping("/api/items/{itemId}/recipe-items")
public class RecipeItemController {

    private final RecipeItemService recipeItemService;

    public RecipeItemController(RecipeItemService recipeItemService) {
        this.recipeItemService = recipeItemService;
    }

    @GetMapping
    public List<RecipeItem> list(@PathVariable Long itemId) {
        return recipeItemService.listByItemId(itemId);
    }

    @PostMapping
    public ResponseEntity<RecipeItem> create(
            @PathVariable Long itemId,
            @RequestBody RecipeItem recipeItem) {
        recipeItem.setItemId(itemId);
        RecipeItem created = recipeItemService.createRecipeItem(recipeItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
