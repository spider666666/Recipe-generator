package com.recipe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recipe.entity.Ingredient;
import com.recipe.entity.ShoppingList;
import com.recipe.mapper.IngredientMapper;
import com.recipe.mapper.ShoppingListMapper;
import com.recipe.service.IShoppingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物清单服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListServiceImpl implements IShoppingListService {

    private final ShoppingListMapper shoppingListMapper;
    private final IngredientMapper ingredientMapper;

    @Override
    @Transactional
    public ShoppingList addItem(Long userId, Long ingredientId, String quantity, String note) {
        // 检查是否已存在该食材（未购买的）
        LambdaQueryWrapper<ShoppingList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingList::getUserId, userId)
               .eq(ShoppingList::getIngredientId, ingredientId)
               .eq(ShoppingList::getIsPurchased, false);

        ShoppingList existingItem = shoppingListMapper.selectOne(wrapper);

        if (existingItem != null) {
            // 如果已存在，智能合并用量
            String combinedQuantity = mergeQuantities(existingItem.getQuantity(), quantity);
            existingItem.setQuantity(combinedQuantity);

            // 合并备注
            if (note != null && !note.isEmpty()) {
                String existingNote = existingItem.getNote();
                if (existingNote != null && !existingNote.isEmpty()) {
                    existingItem.setNote(existingNote + "; " + note);
                } else {
                    existingItem.setNote(note);
                }
            }

            shoppingListMapper.updateById(existingItem);
            log.info("用户{}更新购物清单项, 食材ID: {}, 合并用量: {}", userId, ingredientId, combinedQuantity);
            return existingItem;
        } else {
            // 不存在则创建新项
            ShoppingList item = new ShoppingList();
            item.setUserId(userId);
            item.setIngredientId(ingredientId);
            item.setQuantity(quantity);
            item.setNote(note);
            item.setIsPurchased(false);
            shoppingListMapper.insert(item);

            log.info("用户{}添加购物清单项, 食材ID: {}", userId, ingredientId);
            return item;
        }
    }

    /**
     * 智能合并用量：如果单位相同则数字相加，否则用逗号分隔
     */
    private String mergeQuantities(String existingQuantity, String newQuantity) {
        if (newQuantity == null || newQuantity.isEmpty()) {
            return existingQuantity;
        }
        if (existingQuantity == null || existingQuantity.isEmpty()) {
            return newQuantity;
        }

        try {
            // 提取数字和单位
            QuantityParts existing = parseQuantity(existingQuantity);
            QuantityParts newPart = parseQuantity(newQuantity);

            // 如果单位相同（或都没有单位），则数字相加
            if (existing.unit.equals(newPart.unit)) {
                double sum = existing.number + newPart.number;
                // 如果结果是整数，不显示小数点
                if (sum == (long) sum) {
                    return String.format("%d%s", (long) sum, existing.unit);
                } else {
                    return String.format("%.1f%s", sum, existing.unit);
                }
            } else {
                // 单位不同，用逗号分隔
                return existingQuantity + ", " + newQuantity;
            }
        } catch (Exception e) {
            // 解析失败，用逗号分隔
            log.warn("无法解析用量，使用逗号分隔: {} + {}", existingQuantity, newQuantity);
            return existingQuantity + ", " + newQuantity;
        }
    }

    /**
     * 解析用量字符串，提取数字和单位
     */
    private QuantityParts parseQuantity(String quantity) {
        if (quantity == null || quantity.isEmpty()) {
            return new QuantityParts(0, "");
        }

        // 去除空格
        quantity = quantity.trim();

        // 使用正则表达式提取数字部分
        StringBuilder numberStr = new StringBuilder();
        StringBuilder unitStr = new StringBuilder();
        boolean foundNumber = false;

        for (int i = 0; i < quantity.length(); i++) {
            char c = quantity.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == ',' && !foundNumber) {
                numberStr.append(c == ',' ? '.' : c);
                foundNumber = true;
            } else if (foundNumber) {
                unitStr.append(c);
            }
        }

        double number = 0;
        if (numberStr.length() > 0) {
            try {
                number = Double.parseDouble(numberStr.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无法解析数字: " + numberStr);
            }
        }

        return new QuantityParts(number, unitStr.toString().trim());
    }

    /**
     * 用量解析结果
     */
    private static class QuantityParts {
        double number;
        String unit;

        QuantityParts(double number, String unit) {
            this.number = number;
            this.unit = unit;
        }
    }

    @Override
    public List<ShoppingList> getUserShoppingList(Long userId) {
        LambdaQueryWrapper<ShoppingList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingList::getUserId, userId)
               .orderByAsc(ShoppingList::getIsPurchased)
               .orderByDesc(ShoppingList::getCreateTime);

        List<ShoppingList> shoppingList = shoppingListMapper.selectList(wrapper);

        // 加载关联的食材信息
        for (ShoppingList item : shoppingList) {
            Ingredient ingredient = ingredientMapper.selectById(item.getIngredientId());
            item.setIngredient(ingredient);
        }

        return shoppingList;
    }

    @Override
    @Transactional
    public boolean updatePurchaseStatus(Long userId, Long itemId, Boolean isPurchased) {
        LambdaQueryWrapper<ShoppingList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingList::getUserId, userId)
               .eq(ShoppingList::getId, itemId);

        ShoppingList item = shoppingListMapper.selectOne(wrapper);
        if (item == null) {
            return false;
        }

        item.setIsPurchased(isPurchased);
        int updated = shoppingListMapper.updateById(item);
        log.info("用户{}更新购物清单项{}状态为{}", userId, itemId, isPurchased);
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteItem(Long userId, Long itemId) {
        LambdaQueryWrapper<ShoppingList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingList::getUserId, userId)
               .eq(ShoppingList::getId, itemId);

        int deleted = shoppingListMapper.delete(wrapper);
        log.info("用户{}删除购物清单项{}, 删除{}条记录", userId, itemId, deleted);
        return deleted > 0;
    }

    @Override
    @Transactional
    public boolean clearUserShoppingList(Long userId) {
        LambdaQueryWrapper<ShoppingList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingList::getUserId, userId);

        int deleted = shoppingListMapper.delete(wrapper);
        log.info("用户{}清空购物清单, 删除{}条记录", userId, deleted);
        return deleted > 0;
    }
}
