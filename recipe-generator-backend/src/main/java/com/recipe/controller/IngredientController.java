package com.recipe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recipe.dto.response.ApiResponse;
import com.recipe.entity.Ingredient;
import com.recipe.mapper.IngredientMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 食材管理控制器
 */
@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
@Tag(name = "食材管理", description = "食材查询相关接口")
@SecurityRequirement(name = "Bearer Authentication")
public class IngredientController {

    private final IngredientMapper ingredientMapper;

    @GetMapping
    @Operation(summary = "获取所有食材")
    public ApiResponse<List<Ingredient>> getAllIngredients() {
        List<Ingredient> ingredients = ingredientMapper.selectList(null);
        return ApiResponse.success("获取成功", ingredients);
    }

    @GetMapping("/search")
    @Operation(summary = "根据名称搜索食材（支持智能匹配）")
    public ApiResponse<Ingredient> searchByName(@RequestParam String name) {
        // 1. 首先尝试精确匹配
        LambdaQueryWrapper<Ingredient> exactWrapper = new LambdaQueryWrapper<>();
        exactWrapper.eq(Ingredient::getName, name);
        Ingredient ingredient = ingredientMapper.selectOne(exactWrapper);

        if (ingredient != null) {
            return ApiResponse.success("查询成功", ingredient);
        }

        // 2. 精确匹配失败，尝试智能匹配
        // 策略：搜索包含关键词的食材，或者被关键词包含的食材
        LambdaQueryWrapper<Ingredient> fuzzyWrapper = new LambdaQueryWrapper<>();
        fuzzyWrapper.like(Ingredient::getName, name)
                   .or()
                   .apply("'{0}' LIKE CONCAT('%', name, '%')", name);

        List<Ingredient> candidates = ingredientMapper.selectList(fuzzyWrapper);

        if (candidates.isEmpty()) {
            return ApiResponse.success("查询成功", null);
        }

        // 3. 如果找到多个候选，返回名称最短的（最可能是基础食材）
        // 例如："大蒜"会匹配到"蒜"，"白糖"会匹配到"糖"
        ingredient = candidates.stream()
                .min((a, b) -> Integer.compare(a.getName().length(), b.getName().length()))
                .orElse(candidates.get(0));

        return ApiResponse.success("查询成功（智能匹配）", ingredient);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取食材")
    public ApiResponse<Ingredient> getById(@PathVariable Long id) {
        Ingredient ingredient = ingredientMapper.selectById(id);
        return ApiResponse.success("获取成功", ingredient);
    }
}
