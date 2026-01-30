package com.recipe.controller;

import com.recipe.dto.request.AddShoppingItemRequest;
import com.recipe.dto.response.ApiResponse;
import com.recipe.entity.ShoppingList;
import com.recipe.entity.User;
import com.recipe.service.IShoppingListService;
import com.recipe.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物清单管理控制器
 */
@RestController
@RequestMapping("/shopping-list")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "购物清单管理", description = "购物清单相关接口")
@SecurityRequirement(name = "Bearer Authentication")
public class ShoppingListController {

    private final IShoppingListService shoppingListService;
    private final IUserService userService;

    @PostMapping
    @Operation(summary = "添加购物清单项")
    public ApiResponse<ShoppingList> addItem(@Valid @RequestBody AddShoppingItemRequest request) {
        try {
            Long userId = getCurrentUserId();
            ShoppingList item = shoppingListService.addItem(
                userId,
                request.getIngredientId(),
                request.getQuantity(),
                request.getNote()
            );
            return ApiResponse.success("添加成功", item);
        } catch (Exception e) {
            log.error("添加购物清单项失败: ingredientId={}, quantity={}, error={}",
                request.getIngredientId(), request.getQuantity(), e.getMessage(), e);
            return ApiResponse.error("添加失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取我的购物清单")
    public ApiResponse<List<ShoppingList>> getMyShoppingList() {
        Long userId = getCurrentUserId();
        List<ShoppingList> shoppingList = shoppingListService.getUserShoppingList(userId);
        return ApiResponse.success("获取成功", shoppingList);
    }

    @PutMapping("/{itemId}/purchase")
    @Operation(summary = "更新购买状态")
    public ApiResponse<Void> updatePurchaseStatus(
        @PathVariable Long itemId,
        @RequestParam Boolean isPurchased
    ) {
        Long userId = getCurrentUserId();
        boolean success = shoppingListService.updatePurchaseStatus(userId, itemId, isPurchased);
        if (success) {
            return ApiResponse.success("更新成功", null);
        } else {
            return ApiResponse.error("更新失败");
        }
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "删除购物清单项")
    public ApiResponse<Void> deleteItem(@PathVariable Long itemId) {
        Long userId = getCurrentUserId();
        boolean success = shoppingListService.deleteItem(userId, itemId);
        if (success) {
            return ApiResponse.success("删除成功", null);
        } else {
            return ApiResponse.error("删除失败");
        }
    }

    @DeleteMapping
    @Operation(summary = "清空购物清单")
    public ApiResponse<Void> clearShoppingList() {
        Long userId = getCurrentUserId();
        boolean success = shoppingListService.clearUserShoppingList(userId);
        if (success) {
            return ApiResponse.success("清空成功", null);
        } else {
            return ApiResponse.error("清空失败");
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return user.getId();
    }
}
