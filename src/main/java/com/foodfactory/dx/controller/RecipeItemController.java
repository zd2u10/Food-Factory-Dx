package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.RecipeItem;
import com.foodfactory.dx.service.RecipeItemService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 複数のレシピ明細を一括登録する。
     * リクエストボディはRecipeItemの配列(JSON配列)を受け取る。
     * URLの{itemId}を、リストの全要素に共通のitemIdとして設定してから登録する。
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<RecipeItem>> createBulk(
            @PathVariable Long itemId,
            @RequestBody List<RecipeItem> recipeItems) {
        recipeItems.forEach(line -> line.setItemId(itemId));
        List<RecipeItem> created = recipeItemService.createRecipeItemsBulk(recipeItems);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** レシピ明細を編集する(材料の変更を含む)。 */
    @PutMapping("/{recipeItemId}")
    public ResponseEntity<RecipeItem> update(
            @PathVariable Long itemId,
            @PathVariable Long recipeItemId,
            @RequestBody RecipeItem recipeItem) {
        recipeItem.setItemId(itemId);
        RecipeItem updated = recipeItemService.updateRecipeItem(recipeItemId, recipeItem);
        return ResponseEntity.ok(updated);
    }

    /** レシピ明細を削除する(誤って登録した明細の取り消し)。 */
    @DeleteMapping("/{recipeItemId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long itemId,
            @PathVariable Long recipeItemId) {
        recipeItemService.deleteRecipeItem(recipeItemId);
        return ResponseEntity.noContent().build();
    }
}
