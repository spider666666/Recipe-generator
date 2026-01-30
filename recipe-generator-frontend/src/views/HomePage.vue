<template>
  <div class="home-page">
    <div class="title-container">
      <img src="@/assets/images/厨师猫.png" alt="厨师猫" class="title-cat-left" />
      <h1 class="page-title">🍳 智能食谱生成器</h1>
      <img src="@/assets/images/厨师猫.png" alt="厨师猫" class="title-cat-right" />
    </div>

    <div class="layout-container">
      <!-- 左侧：食材选择区 -->
      <div class="left-section">
        <!-- 搜索框 -->
        <el-input
          v-model="searchQuery"
          placeholder="🔍 搜索食材..."
          clearable
          size="large"
          class="search-input"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <!-- 搜索结果 -->
        <div v-if="searchQuery && filteredIngredients.length > 0" class="search-results">
          <h3>搜索结果</h3>
          <IngredientGrid :ingredients="filteredIngredients" @select="handleSelectIngredient" />
        </div>

        <!-- 食材分类标签页 -->
        <el-tabs v-model="activeCategory" class="ingredient-tabs">
          <el-tab-pane label="🥬 蔬菜类" name="vegetables">
            <IngredientGrid :ingredients="ingredientsByCategory.vegetables" @select="handleSelectIngredient" />
          </el-tab-pane>
          <el-tab-pane label="🥩 肉类" name="meat">
            <IngredientGrid :ingredients="ingredientsByCategory.meat" @select="handleSelectIngredient" />
          </el-tab-pane>
          <el-tab-pane label="🦐 海鲜类" name="seafood">
            <IngredientGrid :ingredients="ingredientsByCategory.seafood" @select="handleSelectIngredient" />
          </el-tab-pane>
          <el-tab-pane label="🍚 主食类" name="staple">
            <IngredientGrid :ingredients="ingredientsByCategory.staple" @select="handleSelectIngredient" />
          </el-tab-pane>
          <!-- 调味料默认具备，不需要选择 -->
          <!-- <el-tab-pane label="🧂 调味料" name="seasoning">
            <IngredientGrid :ingredients="ingredientsByCategory.seasoning" @select="handleSelectIngredient" />
          </el-tab-pane> -->
        </el-tabs>

        <!-- 自由输入 -->
        <el-card class="custom-input-card">
          <template #header>
            <div class="card-header-with-cat">
              <span>✨ 添加自定义食材</span>
              <img src="@/assets/images/猫爪.png" alt="猫爪" class="card-cat-icon" />
            </div>
          </template>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-input v-model="customIngredient" placeholder="输入未列出的食材" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="customAmount" placeholder="数量（如：200g）" />
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="addCustomIngredient" class="add-ingredient-btn">添加</el-button>
            </el-col>
          </el-row>
        </el-card>

        <!-- 常用组合 -->
        <el-card v-if="savedCombos.length > 0" class="combos-card">
          <template #header>
            <div class="card-header">
              <span>💾 常用组合</span>
              <span class="combo-count">{{ savedCombos.length }} 个</span>
            </div>
          </template>
          <div class="combos-grid">
            <div
              v-for="combo in savedCombos"
              :key="combo.createdAt"
              class="combo-item"
            >
              <div class="combo-item-header">
                <el-icon class="combo-icon"><Collection /></el-icon>
                <span class="combo-name">{{ combo.name }}</span>
              </div>
              <div class="combo-item-body">
                <span class="combo-count-badge">{{ combo.ingredients.length }} 种食材</span>
              </div>
              <div class="combo-item-actions">
                <el-button type="primary" size="small" @click="loadCombo(combo)" class="combo-use-btn">
                  <el-icon><Check /></el-icon>
                  使用
                </el-button>
                <el-button type="danger" size="small" plain @click="deleteCombo(combo)" class="combo-delete-btn">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：已选食材和筛选器 -->
      <div class="right-section">
        <div class="sticky-container">
          <!-- 已选食材 -->
          <el-card class="selected-card">
            <template #header>
              <div class="card-header">
                <span>🛒 已选食材</span>
                <el-button type="primary" link @click="saveCombo" size="small">
                  <el-icon><Collection /></el-icon>
                  保存
                </el-button>
              </div>
            </template>
            <div v-if="selectedIngredients.length === 0" class="empty-hint">
              <img src="@/assets/images/困惑猫.png" alt="困惑猫" class="empty-cat-icon" />
              <div>还没有选择食材喵~</div>
            </div>
            <el-space v-else wrap class="ingredients-list">
              <el-tag
                v-for="(item, index) in selectedIngredients"
                :key="index"
                closable
                @close="removeIngredient(index)"
                size="large"
                type="success"
              >
                {{ item.name }} {{ item.amount }}
              </el-tag>
            </el-space>
          </el-card>

          <!-- 筛选器 -->
          <el-card class="filters-card">
            <template #header>
              <div class="card-header-with-cat">
                <span>⚙️ 筛选条件</span>
                <img src="@/assets/images/猫爪.png" alt="猫爪" class="card-cat-icon" />
              </div>
            </template>

            <el-form label-position="top">
              <el-form-item label="菜系">
                <el-select v-model="filters.cuisine" placeholder="请选择" style="width: 100%">
                  <el-option label="不限" value="" />
                  <el-option label="中餐" value="chinese" />
                  <el-option label="西餐" value="western" />
                  <el-option label="日韩料理" value="japanese" />
                  <el-option label="东南亚菜" value="southeast" />
                </el-select>
              </el-form-item>

              <el-form-item label="口味">
                <el-checkbox-group v-model="filters.tastes" class="taste-group">
                  <el-checkbox label="spicy">🌶️ 辣</el-checkbox>
                  <el-checkbox label="sweet">🍯 甜</el-checkbox>
                  <el-checkbox label="salty">🧂 咸</el-checkbox>
                  <el-checkbox label="sour">🍋 酸</el-checkbox>
                  <el-checkbox label="light">🌿 清淡</el-checkbox>
                </el-checkbox-group>
              </el-form-item>

              <el-form-item label="烹饪时间">
                <el-radio-group v-model="filters.time" class="time-group">
                  <el-radio label="15">15分钟</el-radio>
                  <el-radio label="30">30分钟</el-radio>
                  <el-radio label="60">1小时</el-radio>
                  <el-radio label="">不限</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="难度">
                <el-radio-group v-model="filters.difficulty" class="difficulty-group">
                  <el-radio label="easy">新手</el-radio>
                  <el-radio label="medium">家常</el-radio>
                  <el-radio label="hard">大厨</el-radio>
                  <el-radio label="">不限</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 生成按钮 -->
          <el-button
            type="primary"
            size="large"
            class="generate-btn"
            @click="generateRecipes"
            :loading="loading"
            :disabled="selectedIngredients.length === 0"
          >
            <img v-if="loading" src="@/assets/images/厨师小猫在搅拌锅.png" alt="加载中" class="btn-cat-icon loading-cat" />
            <img v-else src="@/assets/images/开心猫.png" alt="开心猫" class="btn-cat-icon" />
            {{ loading ? '生成中...' : '生成食谱喵~' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 背景装饰猫爪 -->
    <div class="paw-decoration paw-1"></div>
    <div class="paw-decoration paw-2"></div>
    <div class="paw-decoration paw-3"></div>
    <div class="paw-decoration paw-4"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Collection, Close, Check, Delete } from '@element-plus/icons-vue'
import IngredientGrid from '../components/IngredientGrid.vue'
import { generateRecipesAPI, addHistoryAPI, getCombosAPI, saveCombosAPI, deleteComboAPI } from '../utils/api'
import { ingredientsData } from '../utils/ingredientsData'

const activeCategory = ref('vegetables')
const searchQuery = ref('')
const customIngredient = ref('')
const customAmount = ref('')
const selectedIngredients = ref([])
const loading = ref(false)
const savedCombos = ref([])

const filters = ref({
  cuisine: 'chinese',  // 默认中餐
  tastes: [],
  time: '30',  // 默认30分钟
  difficulty: 'medium'  // 默认家常
})

// 加载保存的组合
onMounted(() => {
  loadSavedCombos()
})

const loadSavedCombos = async () => {
  try {
    const response = await getCombosAPI()
    if (response.data) {
      savedCombos.value = response.data.map(combo => ({
        id: combo.id,
        name: combo.name,
        ingredients: JSON.parse(combo.ingredients),
        createdAt: combo.createTime
      }))
    }
  } catch (error) {
    console.error('加载组合失败:', error)
  }
}

// 食材数据按分类
const ingredientsByCategory = ingredientsData

// 所有食材列表
const allIngredients = computed(() => {
  return Object.values(ingredientsByCategory).flat()
})

// 搜索过滤
const filteredIngredients = computed(() => {
  if (!searchQuery.value) return []
  return allIngredients.value.filter(ing =>
    ing.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 选择食材
const handleSelectIngredient = (ingredient) => {
  const exists = selectedIngredients.value.find(item => item.name === ingredient.name)
  if (exists) {
    ElMessage.warning('该食材已添加')
    return
  }
  selectedIngredients.value.push({
    name: ingredient.name,
    amount: ingredient.defaultAmount || ''
  })
  ElMessage.success(`已添加 ${ingredient.name}`)
}

// 添加自定义食材
const addCustomIngredient = () => {
  if (!customIngredient.value) {
    ElMessage.warning('请输入食材名称')
    return
  }
  const exists = selectedIngredients.value.find(item => item.name === customIngredient.value)
  if (exists) {
    ElMessage.warning('该食材已添加')
    return
  }
  selectedIngredients.value.push({
    name: customIngredient.value,
    amount: customAmount.value
  })
  ElMessage.success(`已添加 ${customIngredient.value}`)
  customIngredient.value = ''
  customAmount.value = ''
}

// 移除食材
const removeIngredient = (index) => {
  selectedIngredients.value.splice(index, 1)
}

// 保存常用组合
const saveCombo = async () => {
  if (selectedIngredients.value.length === 0) {
    ElMessage.warning('请先选择食材喵~')
    return
  }

  const { value: name } = await ElMessageBox.prompt(
    '给这个食材组合起个名字吧，方便下次快速使用喵~',
    '💾 保存组合',
    {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：家常菜组合',
      customClass: 'cat-message-box'
    }
  ).catch(() => {})

  if (name) {
    try {
      await saveCombosAPI({
        name,
        ingredients: JSON.stringify(selectedIngredients.value)
      })
      await loadSavedCombos()  // 重新加载组合列表
      ElMessage.success('保存成功喵~')
    } catch (error) {
      ElMessage.error(error.message || '保存失败，请重试喵~')
    }
  }
}

// 加载组合
const loadCombo = (combo) => {
  selectedIngredients.value = [...combo.ingredients]
  ElMessage.success(`已加载组合：${combo.name}`)
}

// 删除组合
const deleteCombo = async (combo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除组合"${combo.name}"吗？删除后无法恢复喵~`,
      '🗑️ 删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '再想想',
        type: 'warning',
        customClass: 'cat-message-box'
      }
    )

    await deleteComboAPI(combo.id)
    await loadSavedCombos()  // 重新加载组合列表
    ElMessage.success('已删除喵~')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败，请重试喵~')
    }
  }
}

// 搜索处理
const handleSearch = () => {
  // 实时过滤已在 computed 中处理
}

// 生成食谱
const generateRecipes = async () => {
  if (selectedIngredients.value.length === 0) {
    ElMessage.warning('请至少选择一种食材')
    return
  }

  loading.value = true
  try {
    // 映射前端值到后端枚举
    const cuisineMap = {
      'chinese': 'CHINESE',
      'western': 'WESTERN',
      'japanese': 'JAPANESE_KOREAN',
      'southeast': 'SOUTHEAST_ASIAN'
    }

    const flavorMap = {
      'spicy': 'SPICY',
      'sweet': 'SWEET',
      'salty': 'SALTY',
      'sour': 'SOUR',
      'light': 'MILD'
    }

    const difficultyMap = {
      'easy': 'BEGINNER',
      'medium': 'HOME_COOKING',
      'hard': 'CHEF'
    }

    // 构建符合后端要求的请求参数
    const params = {
      ingredients: selectedIngredients.value.map(ing => ({
        name: ing.name,
        quantity: ing.amount || '适量'  // 后端字段名是 quantity
      })),
      cuisineType: cuisineMap[filters.value.cuisine] || 'CHINESE',  // 默认中餐
      flavorTypes: filters.value.tastes.map(taste => flavorMap[taste]).filter(Boolean),
      cookingTime: parseInt(filters.value.time) || 30,  // 默认30分钟
      difficultyLevel: difficultyMap[filters.value.difficulty] || 'HOME_COOKING'  // 默认家常
    }

    const response = await generateRecipesAPI(params)

    // 后端返回格式: { code, message, data: List<Recipe> }
    // 提取实际的菜谱数据并转换为前端格式
    const recipes = response.data

    // 获取用户选择的食材名称列表
    const userIngredientNames = selectedIngredients.value.map(ing => ing.name)

    // 转换所有食谱的格式
    const recipesArray = recipes.map(recipe => ({
      id: recipe.id || Date.now() + Math.random(),
      name: recipe.name,
      cuisine: recipe.cuisineType,  // 后端: cuisineType
      time: recipe.cookingTime,     // 后端: cookingTime
      difficulty: recipe.difficultyLevel,  // 后端: difficultyLevel
      description: recipe.description,
      servings: recipe.servings,
      ingredients: recipe.ingredients?.map(ing => ({
        name: ing.name,
        amount: ing.quantity,  // 后端: quantity
        // 根据用户选择判断是否具备
        available: userIngredientNames.includes(ing.name)
      })) || [],
      steps: recipe.steps || [],
      missingIngredients: recipe.missingIngredients || [],
      matchScore: recipe.matchScore
    }))

    // 保存每个食谱到后端历史记录
    try {
      for (const recipe of recipes) {
        await addHistoryAPI(recipe.id)
      }
    } catch (err) {
      console.error('保存历史记录失败:', err)
    }

    ElMessage.success(`成功生成${recipesArray.length}个食谱！`)

    // 跳转到菜谱页面
    setTimeout(() => {
      window.dispatchEvent(new CustomEvent('navigate', { detail: 'recipes' }))
    }, 500)
  } catch (error) {
    ElMessage.error(error.message || '生成失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.home-page {
  padding: 30px 20px;
  max-width: 1600px;
  margin: 0 auto;
  min-height: 100vh;
  background:
    radial-gradient(circle at 20% 50%, rgba(255, 179, 120, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 140, 158, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 40% 20%, rgba(255, 200, 124, 0.08) 0%, transparent 50%),
    linear-gradient(135deg, #fff8f0 0%, #ffe8f0 50%, #fff5e8 100%);
  position: relative;
  overflow-x: hidden;
}

/* 标题容器 */
.title-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
  animation: slideDown 0.6s ease-out;
}

.title-cat-left,
.title-cat-right {
  width: 60px;
  height: 60px;
  object-fit: contain;
  animation: bounce 2s ease-in-out infinite;
}

.title-cat-right {
  transform: scaleX(-1);
  animation-delay: 0.3s;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.page-title {
  font-size: 42px;
  font-weight: 800;
  text-align: center;
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b9d 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -1px;
  margin: 0;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左右分栏布局 */
.layout-container {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

/* 左侧食材选择区 */
.left-section {
  flex: 1;
  min-width: 0;
  animation: fadeInLeft 0.6s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 右侧固定区域 */
.right-section {
  width: 400px;
  flex-shrink: 0;
  animation: fadeInRight 0.6s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sticky-container {
  position: sticky;
  top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 搜索框 */
.search-input {
  margin-bottom: 20px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 30px;
  box-shadow: 0 4px 20px rgba(255, 140, 158, 0.15);
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background: white;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 6px 24px rgba(255, 140, 158, 0.25);
  border-color: rgba(255, 140, 158, 0.3);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 6px 24px rgba(255, 140, 158, 0.35);
  border-color: #ff8c9e;
}

/* 搜索结果 */
.search-results {
  margin-bottom: 20px;
  padding: 25px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.search-results h3 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

/* 食材标签页 */
.ingredient-tabs {
  margin-bottom: 20px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.ingredient-tabs:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.ingredient-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.ingredient-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.ingredient-tabs :deep(.el-tabs__item:hover) {
  color: #ff8c69;
}

.ingredient-tabs :deep(.el-tabs__item.is-active) {
  color: #ff8c69;
}

.ingredient-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #ff8c69 0%, #ff6b9d 100%);
  height: 3px;
}

/* 卡片通用样式 */
.custom-input-card,
.combos-card,
.selected-card,
.filters-card {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
  background: white;
}

.custom-input-card,
.combos-card {
  margin-bottom: 20px;
}

.custom-input-card:hover,
.combos-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.selected-card:hover,
.filters-card:hover {
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.12);
}

.custom-input-card :deep(.el-card__header),
.combos-card :deep(.el-card__header),
.selected-card :deep(.el-card__header),
.filters-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(255, 179, 120, 0.12) 0%, rgba(255, 140, 158, 0.12) 100%);
  border-bottom: 2px solid rgba(255, 140, 158, 0.2);
  font-weight: 600;
  font-size: 15px;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 卡片装饰猫爪 */
.card-header-with-cat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.card-cat-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.card-cat-icon:hover {
  opacity: 1;
  transform: rotate(15deg) scale(1.1);
}

/* 自定义食材添加按钮 */
.add-ingredient-btn {
  width: 100%;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b9d 100%);
  border: none;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 140, 158, 0.3);
}

.add-ingredient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 140, 158, 0.45);
}

.add-ingredient-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255, 140, 158, 0.3);
}

/* 常用组合 - 卡片网格布局 */
.combo-count {
  font-size: 13px;
  color: #909399;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.combos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.combo-item {
  background: linear-gradient(135deg, #fff8f0 0%, #ffe8f0 100%);
  border: 2px solid rgba(255, 140, 158, 0.2);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.combo-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ff8c69 0%, #ff6b9d 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.combo-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(255, 140, 158, 0.3);
  border-color: rgba(255, 140, 158, 0.4);
}

.combo-item:hover::before {
  transform: scaleX(1);
}

.combo-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.combo-icon {
  color: #ff8c69;
  font-size: 20px;
  flex-shrink: 0;
}

.combo-name {
  flex: 1;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.combo-item-body {
  display: flex;
  justify-content: flex-start;
}

.combo-count-badge {
  font-size: 12px;
  color: #909399;
  background: rgba(255, 255, 255, 0.8);
  padding: 4px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.combo-item-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.combo-use-btn,
.combo-delete-btn {
  flex: 1;
  font-size: 13px;
  height: 32px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.combo-use-btn {
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b9d 100%);
  border: none;
  color: white;
}

.combo-use-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 140, 158, 0.4);
}

.combo-delete-btn {
  border-color: #f56c6c;
  color: #f56c6c;
}

.combo-delete-btn:hover {
  background: #f56c6c;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
}

/* 已选食材 */
.empty-hint {
  color: #909399;
  text-align: center;
  padding: 40px 0;
  font-size: 14px;
}

.empty-cat-icon {
  width: 80px;
  height: 80px;
  object-fit: contain;
  margin-bottom: 15px;
  animation: wiggle 2s ease-in-out infinite;
}

@keyframes wiggle {
  0%, 100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-5deg);
  }
  75% {
    transform: rotate(5deg);
  }
}

.ingredients-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 5px;
}

.ingredients-list::-webkit-scrollbar {
  width: 6px;
}

.ingredients-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.ingredients-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.selected-card :deep(.el-tag) {
  margin: 4px;
  padding: 10px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 2px solid currentColor;
}

.selected-card :deep(.el-tag:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

/* 筛选器 */
.filters-card :deep(.el-form) {
  margin-top: -5px;
}

.filters-card :deep(.el-form-item) {
  margin-bottom: 22px;
}

.filters-card :deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  margin-bottom: 10px;
  padding: 0;
}

.filters-card :deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.filters-card :deep(.el-select .el-input__wrapper:hover) {
  border-color: #ff8c69;
}

.taste-group,
.time-group,
.difficulty-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filters-card :deep(.el-checkbox),
.filters-card :deep(.el-radio) {
  margin-right: 0;
  margin-bottom: 0;
  font-weight: 500;
  font-size: 13px;
  padding: 8px 14px;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: #f5f7fa;
}

.filters-card :deep(.el-checkbox:hover),
.filters-card :deep(.el-radio:hover) {
  background: #e8eef5;
}

.filters-card :deep(.el-checkbox.is-checked),
.filters-card :deep(.el-radio.is-checked) {
  background: rgba(255, 140, 158, 0.15);
}

/* 生成按钮 */
.generate-btn {
  width: 100%;
  height: 60px;
  font-size: 18px;
  font-weight: 700;
  border-radius: 30px;
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b9d 100%);
  border: none;
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.4);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-cat-icon {
  width: 28px;
  height: 28px;
  object-fit: contain;
  animation: rotate 3s linear infinite;
}

.loading-cat {
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes rotate {
  0%, 90% {
    transform: rotate(0deg);
  }
  95% {
    transform: rotate(15deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

.generate-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.generate-btn:hover:not(:disabled)::before {
  left: 100%;
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(255, 107, 107, 0.5);
}

.generate-btn:active:not(:disabled) {
  transform: translateY(-1px);
}

.generate-btn:disabled {
  background: linear-gradient(135deg, #c0c4cc 0%, #a8abb2 100%);
  box-shadow: none;
  transform: none;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 背景装饰猫爪 */
.paw-decoration {
  position: fixed;
  width: 80px;
  height: 80px;
  background-image: url('@/assets/images/猫爪.png');
  background-size: contain;
  background-repeat: no-repeat;
  opacity: 0.08;
  pointer-events: none;
  z-index: 0;
}

.paw-1 {
  top: 15%;
  left: 5%;
  transform: rotate(-15deg);
  animation: float 6s ease-in-out infinite;
}

.paw-2 {
  top: 60%;
  right: 8%;
  transform: rotate(25deg);
  animation: float 7s ease-in-out infinite 1s;
}

.paw-3 {
  bottom: 20%;
  left: 10%;
  transform: rotate(45deg);
  animation: float 8s ease-in-out infinite 2s;
}

.paw-4 {
  top: 35%;
  right: 15%;
  transform: rotate(-30deg);
  animation: float 9s ease-in-out infinite 3s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(var(--rotation, 0deg));
  }
  50% {
    transform: translateY(-20px) rotate(var(--rotation, 0deg));
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .right-section {
    width: 360px;
  }
}

@media (max-width: 992px) {
  .layout-container {
    flex-direction: column;
  }

  .right-section {
    width: 100%;
  }

  .sticky-container {
    position: static;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 20px 15px;
  }

  .page-title {
    font-size: 32px;
    margin-bottom: 30px;
  }

  .layout-container {
    gap: 20px;
  }

  .ingredient-tabs,
  .search-results,
  .custom-input-card,
  .combos-card {
    padding: 15px;
  }

  .combos-grid {
    grid-template-columns: 1fr;
  }

  .generate-btn {
    height: 55px;
    font-size: 16px;
  }

  .filters-card :deep(.el-checkbox),
  .filters-card :deep(.el-radio) {
    font-size: 12px;
    padding: 6px 10px;
  }
}
</style>
