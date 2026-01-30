package com.recipe.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.recipe.enums.CuisineType;
import com.recipe.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("recipe")
public class Recipe {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private CuisineType cuisineType;

    private String flavorTypes;

    private Integer cookingTime;

    private DifficultyLevel difficultyLevel;

    private String description;

    private String imageUrl;

    private Integer servings;

    @TableField(exist = false)
    private List<RecipeIngredient> recipeIngredients;

    @TableField(exist = false)
    private List<RecipeStep> steps;

    // 用于前端展示的简化食材列表
    @TableField(exist = false)
    private List<IngredientDTO> ingredients;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 内部类：用于前端展示的食材DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDTO {
        private Long ingredientId;  // 食材ID，用于直接添加到购物清单
        private String name;
        private String quantity;
        private Boolean isRequired;
    }
}
