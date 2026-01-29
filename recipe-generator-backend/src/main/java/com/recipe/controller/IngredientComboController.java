package com.recipe.controller;

import com.recipe.dto.request.SaveComboRequest;
import com.recipe.dto.response.ApiResponse;
import com.recipe.entity.IngredientCombo;
import com.recipe.entity.User;
import com.recipe.service.IIngredientComboService;
import com.recipe.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 食材组合管理控制器
 */
@RestController
@RequestMapping("/combos")
@RequiredArgsConstructor
@Tag(name = "食材组合管理", description = "常用食材组合相关接口")
@SecurityRequirement(name = "Bearer Authentication")
public class IngredientComboController {

    private final IIngredientComboService comboService;
    private final IUserService userService;

    @PostMapping
    @Operation(summary = "保存食材组合")
    public ApiResponse<IngredientCombo> saveCombo(@Valid @RequestBody SaveComboRequest request) {
        Long userId = getCurrentUserId();
        IngredientCombo combo = comboService.saveCombo(userId, request.getName(), request.getIngredients());
        return ApiResponse.success("保存成功", combo);
    }

    @GetMapping
    @Operation(summary = "获取我的食材组合")
    public ApiResponse<List<IngredientCombo>> getMyCombos() {
        Long userId = getCurrentUserId();
        List<IngredientCombo> combos = comboService.getUserCombos(userId);
        return ApiResponse.success("获取成功", combos);
    }

    @DeleteMapping("/{comboId}")
    @Operation(summary = "删除食材组合")
    public ApiResponse<Void> deleteCombo(@PathVariable Long comboId) {
        Long userId = getCurrentUserId();
        boolean success = comboService.deleteCombo(userId, comboId);
        if (success) {
            return ApiResponse.success("删除成功", null);
        } else {
            return ApiResponse.error("删除失败");
        }
    }

    @PutMapping("/{comboId}")
    @Operation(summary = "更新食材组合")
    public ApiResponse<Void> updateCombo(
        @PathVariable Long comboId,
        @Valid @RequestBody SaveComboRequest request
    ) {
        Long userId = getCurrentUserId();
        boolean success = comboService.updateCombo(userId, comboId, request.getName(), request.getIngredients());
        if (success) {
            return ApiResponse.success("更新成功", null);
        } else {
            return ApiResponse.error("更新失败");
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return user.getId();
    }
}
