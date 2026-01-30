package com.recipe.controller;

import com.recipe.dto.request.GenerateRecipeRequest;
import com.recipe.dto.response.ApiResponse;
import com.recipe.dto.response.RecipeResponse;
import com.recipe.entity.Recipe;
import com.recipe.entity.User;
import com.recipe.mapper.RecipeMapper;
import com.recipe.service.IClaudeRecipeGeneratorService;
import com.recipe.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
@Tag(name = "食谱管理", description = "食谱生成、查询、删除")
@SecurityRequirement(name = "Bearer Authentication")
public class RecipeController {

    private final IClaudeRecipeGeneratorService recipeGeneratorService;
    private final RecipeMapper recipeMapper;
    private final IUserService userService;

    @PostMapping("/generate")
    @Operation(summary = "生成食谱推荐")
    public ApiResponse<?> generateRecipe(@Valid @RequestBody GenerateRecipeRequest request) {
        try {
            var recipe = recipeGeneratorService.generateRecipeWithClaude(request);
            return ApiResponse.success("生成成功", recipe);
        } catch (Exception e) {
            return ApiResponse.error("生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取食谱详情")
    public ApiResponse<RecipeResponse> getRecipe(@PathVariable Long id) {
        // TODO: 实现获取食谱详情
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除食谱")
    public ApiResponse<Void> deleteRecipe(@PathVariable Long id) {
        try {
            // 删除食谱（级联删除会自动删除关联的食材和步骤）
            int deleted = recipeMapper.deleteById(id);
            if (deleted > 0) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error("食谱不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return user.getId();
    }
}
