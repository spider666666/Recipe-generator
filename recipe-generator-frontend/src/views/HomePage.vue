<template>
  <div class="home-page">
    <el-container>
      <el-main>
        <h1 class="page-title">选择你的食材</h1>

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
          <el-tab-pane label="🧂 调味料" name="seasoning">
            <IngredientGrid :ingredients="ingredientsByCategory.seasoning" @select="handleSelectIngredient" />
          </el-tab-pane>
        </el-tabs>

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

        <!-- 自由输入 -->
        <el-card class="custom-input-card">
          <template #header>
            <span>添加自定义食材</span>
          </template>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-input v-model="customIngredient" placeholder="输入未列出的食材" />
            </el-col>
            <el-col :span="8">
              <el-input v-model="customAmount" placeholder="数量（如：200g）" />
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="addCustomIngredient" style="width: 100%">添加</el-button>
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
          <div class="combos-list">
            <el-tag
              v-for="combo in savedCombos"
              :key="combo.createdAt"
              class="combo-tag"
              size="large"
              type="success"
              effect="plain"
              closable
              @click="loadCombo(combo)"
              @close="deleteCombo(combo)"
            >
              <el-icon><Collection /></el-icon>
              {{ combo.name }} ({{ combo.ingredients.length }}种)
            </el-tag>
          </div>
        </el-card>

        <!-- 已选食材 -->
        <el-card class="selected-card">
          <template #header>
            <div class="card-header">
              <span>已选食材</span>
              <el-button type="primary" link @click="saveCombo">
                <el-icon><Collection /></el-icon>
                保存常用组合
              </el-button>
            </div>
          </template>
          <div v-if="selectedIngredients.length === 0" class="empty-hint">
            还没有选择食材
          </div>
          <el-space v-else wrap>
            <el-tag
              v-for="(item, index) in selectedIngredients"
              :key="index"
              closable
              @close="removeIngredient(index)"
              size="large"
            >
              {{ item.name }} {{ item.amount }}
            </el-tag>
          </el-space>
        </el-card>

        <!-- 筛选器 -->
        <el-card class="filters-card">
          <template #header>
            <span>筛选条件</span>
          </template>

          <el-form label-width="80px">
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
              <el-checkbox-group v-model="filters.tastes">
                <el-checkbox label="spicy">🌶️ 辣</el-checkbox>
                <el-checkbox label="sweet">🍯 甜</el-checkbox>
                <el-checkbox label="salty">🧂 咸</el-checkbox>
                <el-checkbox label="sour">🍋 酸</el-checkbox>
                <el-checkbox label="light">🌿 清淡</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="烹饪时间">
              <el-radio-group v-model="filters.time">
                <el-radio label="15">15分钟</el-radio>
                <el-radio label="30">30分钟</el-radio>
                <el-radio label="60">1小时</el-radio>
                <el-radio label="">不限</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="难度">
              <el-radio-group v-model="filters.difficulty">
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
          <el-icon v-if="!loading"><MagicStick /></el-icon>
          {{ loading ? '生成中...' : '生成食谱' }}
        </el-button>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Collection, MagicStick } from '@element-plus/icons-vue'
import IngredientGrid from '../components/IngredientGrid.vue'
import { generateRecipesAPI } from '../utils/api'
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

const loadSavedCombos = () => {
  savedCombos.value = JSON.parse(localStorage.getItem('ingredient-combos') || '[]')
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
    ElMessage.warning('请先选择食材')
    return
  }

  const { value: name } = await ElMessageBox.prompt('请输入组合名称', '保存组合', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
  }).catch(() => {})

  if (name) {
    const combos = JSON.parse(localStorage.getItem('ingredient-combos') || '[]')
    combos.push({
      name,
      ingredients: [...selectedIngredients.value],
      createdAt: new Date().toISOString()
    })
    localStorage.setItem('ingredient-combos', JSON.stringify(combos))
    loadSavedCombos()  // 重新加载组合列表
    ElMessage.success('保存成功')
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
    await ElMessageBox.confirm(`确定要删除组合"${combo.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const combos = JSON.parse(localStorage.getItem('ingredient-combos') || '[]')
    const filtered = combos.filter(c => c.createdAt !== combo.createdAt)
    localStorage.setItem('ingredient-combos', JSON.stringify(filtered))
    loadSavedCombos()  // 重新加载组合列表
    ElMessage.success('已删除')
  } catch {
    // 用户取消
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

    // 后端返回格式: { code, message, data: Recipe }
    // 提取实际的菜谱数据并转换为前端格式
    const recipe = response.data

    // 获取用户选择的食材名称列表
    const userIngredientNames = selectedIngredients.value.map(ing => ing.name)

    // 转换后端字段名到前端格式
    const formattedRecipe = {
      id: recipe.id || Date.now(),
      name: recipe.name,
      cuisine: recipe.cuisineType,  // 后端: cuisineType
      time: recipe.cookingTime,     // 后端: cookingTime
      difficulty: recipe.difficultyLevel,  // 后端: difficultyLevel
      description: recipe.description,
      servings: recipe.servings,
      ingredients: recipe.ingredients?.map(ing => ({
        name: ing.name,
        amount: ing.quantity,  // 后端: quantity
        // 如果后端提供了 hasIngredient 字段则使用，否则根据用户选择判断
        available: ing.hasIngredient !== undefined
          ? ing.hasIngredient
          : userIngredientNames.includes(ing.name)
      })) || [],
      steps: recipe.steps || [],
      missingIngredients: recipe.missingIngredients || [],
      matchScore: recipe.matchScore
    }

    // 将单个菜谱包装成数组
    const recipesArray = [formattedRecipe]

    // 保存到历史记录
    const history = JSON.parse(localStorage.getItem('recipe-history') || '[]')
    history.unshift({
      id: Date.now(),
      ingredients: [...selectedIngredients.value],
      filters: { ...filters.value },
      recipes: recipesArray,
      createdAt: new Date().toISOString()
    })
    // 只保留最近50条
    if (history.length > 50) history.pop()
    localStorage.setItem('recipe-history', JSON.stringify(history))

    // 保存到全局状态（用于菜谱页面展示）
    localStorage.setItem('current-recipes', JSON.stringify(recipesArray))

    ElMessage.success('食谱生成成功！')

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
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.ingredient-tabs {
  margin-bottom: 20px;
}

.search-input {
  margin-bottom: 20px;
}

.search-results {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.search-results h3 {
  margin-bottom: 15px;
  color: #606266;
}

.custom-input-card,
.combos-card,
.selected-card,
.filters-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.combo-count {
  font-size: 14px;
  color: #909399;
}

.combos-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.combo-tag {
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 16px;
}

.combo-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.combo-tag .el-icon {
  margin-right: 6px;
}

.empty-hint {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

.generate-btn {
  width: 100%;
  height: 60px;
  font-size: 20px;
  margin-top: 20px;
}
</style>
