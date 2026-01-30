<template>
  <div class="recipes-page">
    <el-container>
      <el-main>
        <h1 class="page-title">推荐菜谱</h1>

        <div v-if="recipes.length === 0" class="empty-state">
          <el-empty description="还没有生成菜谱">
            <el-button type="primary" @click="goToHome">去选择食材</el-button>
          </el-empty>
        </div>

        <div v-else class="recipes-grid">
          <el-card
            v-for="recipe in recipes"
            :key="recipe.id"
            class="recipe-card"
            shadow="hover"
          >
            <template #header>
              <div class="card-header">
                <span class="recipe-name">{{ recipe.name }}</span>
                <el-button
                  :icon="isFavorite(recipe.id) ? StarFilled : Star"
                  :type="isFavorite(recipe.id) ? 'warning' : 'default'"
                  circle
                  @click="toggleFavorite(recipe)"
                />
              </div>
            </template>

            <div class="recipe-content">
              <!-- 标签 -->
              <div class="recipe-tags">
                <el-tag v-if="recipe.cuisine" type="info">{{ getCuisineLabel(recipe.cuisine) }}</el-tag>
                <el-tag type="success">{{ recipe.time }}分钟</el-tag>
                <el-tag :type="getDifficultyType(recipe.difficulty)">
                  {{ getDifficultyLabel(recipe.difficulty) }}
                </el-tag>
              </div>

              <!-- 食材清单预览 -->
              <div class="ingredients-preview">
                <h4>所需食材</h4>
                <div class="ingredient-list">
                  <div
                    v-for="(ing, index) in recipe.ingredients.slice(0, 5)"
                    :key="index"
                    class="ingredient-item"
                  >
                    <span :class="ing.available ? 'available' : 'missing'">
                      {{ ing.available ? '✅' : '❌' }}
                    </span>
                    <span>{{ ing.name }} {{ ing.amount }}</span>
                  </div>
                  <div v-if="recipe.ingredients.length > 5" class="more-hint">
                    还有 {{ recipe.ingredients.length - 5 }} 种食材...
                  </div>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="recipe-actions">
                <el-button type="primary" @click="viewDetail(recipe)">
                  查看详情
                </el-button>
                <el-button @click="addToShopping(recipe)">
                  <el-icon><ShoppingCart /></el-icon>
                  加入购物清单
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-main>
    </el-container>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentRecipe?.name"
      width="800px"
      class="recipe-dialog"
    >
      <div v-if="currentRecipe" class="recipe-detail">
        <!-- 标签 -->
        <div class="detail-tags">
          <el-tag v-if="currentRecipe.cuisine" type="info" size="large">
            {{ getCuisineLabel(currentRecipe.cuisine) }}
          </el-tag>
          <el-tag type="success" size="large">{{ currentRecipe.time }}分钟</el-tag>
          <el-tag :type="getDifficultyType(currentRecipe.difficulty)" size="large">
            {{ getDifficultyLabel(currentRecipe.difficulty) }}
          </el-tag>
        </div>

        <!-- 食材清单 -->
        <div class="detail-section">
          <h3>📝 所需食材</h3>
          <el-table :data="currentRecipe.ingredients" style="width: 100%">
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <span :style="{ fontSize: '20px' }">
                  {{ row.available ? '✅' : '❌' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="食材" />
            <el-table-column prop="amount" label="用量" />
          </el-table>
        </div>

        <!-- 烹饪步骤 -->
        <div class="detail-section">
          <h3>👨‍🍳 烹饪步骤</h3>
          <el-steps direction="vertical" :active="currentRecipe.steps.length">
            <el-step
              v-for="(step, index) in currentRecipe.steps"
              :key="index"
              :title="`步骤 ${index + 1}${step.duration ? ` (${step.duration}分钟)` : ''}`"
              :description="step.description || step"
            />
          </el-steps>
        </div>

        <!-- 评价 -->
        <div class="detail-section">
          <h3>⭐ 评价</h3>
          <el-rate
            v-model="currentRecipe.rating"
            @change="saveRating"
            show-text
            :texts="['极差', '失望', '一般', '满意', '惊喜']"
          />
          <el-input
            v-model="currentRecipe.comment"
            type="textarea"
            :rows="3"
            placeholder="写下你的评价..."
            style="margin-top: 10px"
            @blur="saveComment"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="exportRecipe">
          <el-icon><Download /></el-icon>
          导出菜谱
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, ShoppingCart, Download } from '@element-plus/icons-vue'
import { addFavoriteAPI, removeFavoriteAPI, getFavoritesAPI, addShoppingItemAPI, searchIngredientByNameAPI } from '../utils/api'

const recipes = ref([])
const detailVisible = ref(false)
const currentRecipe = ref(null)
const favoriteIds = ref(new Set())

onMounted(() => {
  loadRecipes()
  loadFavorites()
  // 监听导航事件
  window.addEventListener('navigate', handleNavigate)
})

const loadRecipes = () => {
  const stored = localStorage.getItem('current-recipes')
  if (stored) {
    recipes.value = JSON.parse(stored)
  }
}

const loadFavorites = async () => {
  try {
    const response = await getFavoritesAPI()
    if (response.data) {
      favoriteIds.value = new Set(response.data.map(fav => fav.recipeId))
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
  }
}

const handleNavigate = (event) => {
  if (event.detail === 'recipes') {
    loadRecipes()
  }
}

const goToHome = () => {
  window.dispatchEvent(new CustomEvent('navigate', { detail: 'home' }))
}

// 收藏相关
const isFavorite = (id) => {
  return favoriteIds.value.has(id)
}

const toggleFavorite = async (recipe) => {
  try {
    if (isFavorite(recipe.id)) {
      await removeFavoriteAPI(recipe.id)
      favoriteIds.value.delete(recipe.id)
      ElMessage.success('已取消收藏')
    } else {
      await addFavoriteAPI(recipe.id)
      favoriteIds.value.add(recipe.id)
      ElMessage.success('已添加到收藏夹')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 查看详情
const viewDetail = (recipe) => {
  currentRecipe.value = { ...recipe }
  // 加载评价
  const ratings = JSON.parse(localStorage.getItem('recipe-ratings') || '{}')
  if (ratings[recipe.id]) {
    currentRecipe.value.rating = ratings[recipe.id].rating || 0
    currentRecipe.value.comment = ratings[recipe.id].comment || ''
  } else {
    currentRecipe.value.rating = 0
    currentRecipe.value.comment = ''
  }
  detailVisible.value = true
}

// 保存评分
const saveRating = (value) => {
  const ratings = JSON.parse(localStorage.getItem('recipe-ratings') || '{}')
  if (!ratings[currentRecipe.value.id]) {
    ratings[currentRecipe.value.id] = {}
  }
  ratings[currentRecipe.value.id].rating = value
  localStorage.setItem('recipe-ratings', JSON.stringify(ratings))
  ElMessage.success('评分已保存')
}

// 保存评论
const saveComment = () => {
  const ratings = JSON.parse(localStorage.getItem('recipe-ratings') || '{}')
  if (!ratings[currentRecipe.value.id]) {
    ratings[currentRecipe.value.id] = {}
  }
  ratings[currentRecipe.value.id].comment = currentRecipe.value.comment
  localStorage.setItem('recipe-ratings', JSON.stringify(ratings))
  ElMessage.success('评论已保存')
}

// 加入购物清单
const addToShopping = async (recipe) => {
  try {
    let addedCount = 0
    for (const ing of recipe.ingredients) {
      // 查找食材ID
      const ingredientResponse = await searchIngredientByNameAPI(ing.name)
      if (ingredientResponse.data) {
        await addShoppingItemAPI({
          ingredientId: ingredientResponse.data.id,
          quantity: ing.amount,
          note: ''
        })
        addedCount++
      }
    }
    ElMessage.success(`已添加 ${addedCount} 种食材到购物清单`)
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  }
}

// 导出菜谱
const exportRecipe = () => {
  const content = `
菜谱：${currentRecipe.value.name}

菜系：${getCuisineLabel(currentRecipe.value.cuisine)}
时间：${currentRecipe.value.time}分钟
难度：${getDifficultyLabel(currentRecipe.value.difficulty)}

所需食材：
${currentRecipe.value.ingredients.map(ing => `${ing.name} ${ing.amount}`).join('\n')}

烹饪步骤：
${currentRecipe.value.steps.map((step, i) => `${i + 1}. ${step}`).join('\n')}
  `.trim()

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${currentRecipe.value.name}.txt`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}

// 辅助函数
const getCuisineLabel = (cuisine) => {
  const map = {
    // 前端格式
    chinese: '中餐',
    western: '西餐',
    japanese: '日韩料理',
    southeast: '东南亚菜',
    // 后端枚举格式
    CHINESE: '中餐',
    WESTERN: '西餐',
    JAPANESE_KOREAN: '日韩料理',
    SOUTHEAST_ASIAN: '东南亚菜'
  }
  return map[cuisine] || cuisine
}

const getDifficultyLabel = (difficulty) => {
  const map = {
    // 前端格式
    easy: '新手',
    medium: '家常',
    hard: '大厨',
    // 后端枚举格式
    BEGINNER: '新手',
    HOME_COOKING: '家常',
    CHEF: '大厨'
  }
  return map[difficulty] || difficulty
}

const getDifficultyType = (difficulty) => {
  const map = {
    // 前端格式
    easy: 'success',
    medium: 'warning',
    hard: 'danger',
    // 后端枚举格式
    BEGINNER: 'success',
    HOME_COOKING: 'warning',
    CHEF: 'danger'
  }
  return map[difficulty] || 'info'
}
</script>

<style scoped>
.recipes-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.empty-state {
  padding: 60px 0;
}

.recipes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.recipe-card {
  transition: transform 0.3s;
}

.recipe-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recipe-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.recipe-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recipe-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ingredients-preview h4 {
  margin-bottom: 10px;
  color: #606266;
}

.ingredient-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.available {
  color: #67c23a;
}

.missing {
  color: #f56c6c;
}

.more-hint {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.recipe-actions {
  display: flex;
  gap: 10px;
}

.recipe-actions .el-button {
  flex: 1;
}

/* 详情弹窗样式 */
.recipe-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-tags {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #303133;
}
</style>
