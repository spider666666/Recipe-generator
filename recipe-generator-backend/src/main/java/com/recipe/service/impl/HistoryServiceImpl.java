package com.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recipe.entity.History;
import com.recipe.entity.Recipe;
import com.recipe.entity.RecipeIngredient;
import com.recipe.entity.RecipeStep;
import com.recipe.mapper.HistoryMapper;
import com.recipe.mapper.RecipeMapper;
import com.recipe.mapper.RecipeIngredientMapper;
import com.recipe.mapper.RecipeStepMapper;
import com.recipe.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 历史记录服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryServiceImpl implements IHistoryService {

    private final HistoryMapper historyMapper;
    private final RecipeMapper recipeMapper;
    private final RecipeIngredientMapper recipeIngredientMapper;
    private final RecipeStepMapper recipeStepMapper;

    @Override
    @Transactional
    public History addHistory(Long userId, Long recipeId) {
        History history = new History();
        history.setUserId(userId);
        history.setRecipeId(recipeId);
        historyMapper.insert(history);

        log.info("用户{}添加历史记录, 食谱ID: {}", userId, recipeId);
        return history;
    }

    @Override
    public List<History> getUserHistory(Long userId) {
        LambdaQueryWrapper<History> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(History::getUserId, userId)
               .orderByDesc(History::getCreateTime);

        List<History> historyList = historyMapper.selectList(wrapper);

        // 加载关联的食谱信息（包括食材和步骤）
        for (History history : historyList) {
            Recipe recipe = recipeMapper.selectById(history.getRecipeId());

            if (recipe != null) {
                // 加载食材列表
                LambdaQueryWrapper<RecipeIngredient> ingredientWrapper = new LambdaQueryWrapper<>();
                ingredientWrapper.eq(RecipeIngredient::getRecipeId, recipe.getId());
                List<RecipeIngredient> ingredients = recipeIngredientMapper.selectList(ingredientWrapper);
                recipe.setRecipeIngredients(ingredients);

                // 加载步骤列表
                LambdaQueryWrapper<RecipeStep> stepWrapper = new LambdaQueryWrapper<>();
                stepWrapper.eq(RecipeStep::getRecipeId, recipe.getId())
                          .orderByAsc(RecipeStep::getStepNumber);
                List<RecipeStep> steps = recipeStepMapper.selectList(stepWrapper);
                recipe.setSteps(steps);
            }

            history.setRecipe(recipe);
        }

        return historyList;
    }

    @Override
    @Transactional
    public boolean deleteHistory(Long userId, Long historyId) {
        LambdaQueryWrapper<History> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(History::getUserId, userId)
               .eq(History::getId, historyId);

        int deleted = historyMapper.delete(wrapper);
        log.info("用户{}删除历史记录{}, 删除{}条记录", userId, historyId, deleted);
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean clearUserHistory(Long userId) {
        LambdaQueryWrapper<History> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(History::getUserId, userId);

        int deleted = historyMapper.delete(wrapper);
        log.info("用户{}清空历史记录, 删除{}条记录", userId, deleted);
        return deleted > 0;
    }
}
