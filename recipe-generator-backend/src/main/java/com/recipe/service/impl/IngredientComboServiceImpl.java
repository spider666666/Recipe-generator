package com.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recipe.entity.IngredientCombo;
import com.recipe.mapper.IngredientComboMapper;
import com.recipe.service.IIngredientComboService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 食材组合服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientComboServiceImpl implements IIngredientComboService {

    private final IngredientComboMapper comboMapper;

    @Override
    @Transactional
    public IngredientCombo saveCombo(Long userId, String name, String ingredients) {
        IngredientCombo combo = new IngredientCombo();
        combo.setUserId(userId);
        combo.setName(name);
        combo.setIngredients(ingredients);
        comboMapper.insert(combo);

        log.info("用户{}保存食材组合: {}", userId, name);
        return combo;
    }

    @Override
    public List<IngredientCombo> getUserCombos(Long userId) {
        LambdaQueryWrapper<IngredientCombo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IngredientCombo::getUserId, userId)
               .orderByDesc(IngredientCombo::getCreateTime);

        return comboMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean deleteCombo(Long userId, Long comboId) {
        LambdaQueryWrapper<IngredientCombo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IngredientCombo::getUserId, userId)
               .eq(IngredientCombo::getId, comboId);

        int deleted = comboMapper.delete(wrapper);
        log.info("用户{}删除食材组合{}, 删除{}条记录", userId, comboId, deleted);
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean updateCombo(Long userId, Long comboId, String name, String ingredients) {
        LambdaQueryWrapper<IngredientCombo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IngredientCombo::getUserId, userId)
               .eq(IngredientCombo::getId, comboId);

        IngredientCombo combo = comboMapper.selectOne(wrapper);
        if (combo == null) {
            return false;
        }

        combo.setName(name);
        combo.setIngredients(ingredients);
        int updated = comboMapper.updateById(combo);
        log.info("用户{}更新食材组合{}", userId, comboId);
        return updated > 0;
    }
}
