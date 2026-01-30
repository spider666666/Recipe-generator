package com.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recipe.entity.Favorite;
import com.recipe.entity.Ingredient;
import com.recipe.entity.Recipe;
import com.recipe.entity.RecipeIngredient;
import com.recipe.entity.RecipeStep;
import com.recipe.mapper.FavoriteMapper;
import com.recipe.mapper.IngredientMapper;
import com.recipe.mapper.RecipeMapper;
import com.recipe.mapper.RecipeIngredientMapper;
import com.recipe.mapper.RecipeStepMapper;
import com.recipe.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements IFavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final RecipeMapper recipeMapper;
    private final RecipeIngredientMapper recipeIngredientMapper;
    private final RecipeStepMapper recipeStepMapper;
    private final IngredientMapper ingredientMapper;

    @Override
    @Transactional
    public Favorite addFavorite(Long userId, Long recipeId) {
        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getRecipeId, recipeId);

        Favorite existing = favoriteMapper.selectOne(wrapper);
        if (existing != null) {
            log.info("用户{}已收藏食谱{}", userId, recipeId);
            return existing;
        }

        // 创建新收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setRecipeId(recipeId);
        favoriteMapper.insert(favorite);

        log.info("用户{}收藏食谱{}成功", userId, recipeId);
        return favorite;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long recipeId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getRecipeId, recipeId);

        int deleted = favoriteMapper.delete(wrapper);
        log.info("用户{}取消收藏食谱{}, 删除{}条记录", userId, recipeId, deleted);
        return deleted > 0;
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .orderByDesc(Favorite::getCreateTime);

        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        // 加载关联的食谱信息（包括食材和步骤）
        for (Favorite favorite : favorites) {
            Recipe recipe = recipeMapper.selectById(favorite.getRecipeId());

            if (recipe != null) {
                // 加载食材列表
                LambdaQueryWrapper<RecipeIngredient> ingredientWrapper = new LambdaQueryWrapper<>();
                ingredientWrapper.eq(RecipeIngredient::getRecipeId, recipe.getId());
                List<RecipeIngredient> recipeIngredients = recipeIngredientMapper.selectList(ingredientWrapper);
                recipe.setRecipeIngredients(recipeIngredients);

                // 转换为前端期望的格式
                List<Recipe.IngredientDTO> ingredients = new ArrayList<>();
                for (RecipeIngredient ri : recipeIngredients) {
                    Ingredient ingredient = ingredientMapper.selectById(ri.getIngredientId());
                    if (ingredient != null) {
                        Recipe.IngredientDTO dto = new Recipe.IngredientDTO();
                        dto.setName(ingredient.getName());
                        dto.setQuantity(ri.getQuantity());
                        dto.setIsRequired(ri.getIsRequired());
                        ingredients.add(dto);
                    }
                }
                recipe.setIngredients(ingredients);

                // 加载步骤列表
                LambdaQueryWrapper<RecipeStep> stepWrapper = new LambdaQueryWrapper<>();
                stepWrapper.eq(RecipeStep::getRecipeId, recipe.getId())
                          .orderByAsc(RecipeStep::getStepNumber);
                List<RecipeStep> steps = recipeStepMapper.selectList(stepWrapper);
                recipe.setSteps(steps);
            }

            favorite.setRecipe(recipe);
        }

        return favorites;
    }

    @Override
    public boolean isFavorited(Long userId, Long recipeId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getRecipeId, recipeId);

        return favoriteMapper.selectCount(wrapper) > 0;
    }
}
