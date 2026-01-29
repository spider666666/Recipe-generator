package com.recipe.service;

import com.recipe.entity.IngredientCombo;

import java.util.List;

/**
 * 食材组合服务接口
 */
public interface IIngredientComboService {

    /**
     * 保存食材组合
     */
    IngredientCombo saveCombo(Long userId, String name, String ingredients);

    /**
     * 获取用户的所有组合
     */
    List<IngredientCombo> getUserCombos(Long userId);

    /**
     * 删除组合
     */
    boolean deleteCombo(Long userId, Long comboId);

    /**
     * 更新组合
     */
    boolean updateCombo(Long userId, Long comboId, String name, String ingredients);
}
