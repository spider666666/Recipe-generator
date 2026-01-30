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
                <el-button type="danger" @click="deleteRecipe(recipe)">
                  <el-icon><Delete /></el-icon>
                  删除
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, ShoppingCart, Download, Delete } from '@element-plus/icons-vue'
import {
  addFavoriteAPI,
  removeFavoriteAPI,
  getFavoritesAPI,
  addShoppingItemAPI,
  searchIngredientByNameAPI,
  getHistoryAPI,
  deleteRecipeAPI
} from '../utils/api'

const recipes = ref([])
const detailVisible = ref(false)
const currentRecipe = ref(null)
const favoriteIds = ref(new Set())
const addingToCart = ref(new Set())  // 跟踪正在添加到购物车的菜谱ID

onMounted(() => {
  loadRecipes()
  loadFavorites()
  // 监听导航事件
  window.addEventListener('navigate', handleNavigate)
})

const loadRecipes = async () => {
  try {
    const response = await getHistoryAPI()
    if (response.data && response.data.length > 0) {
      // 转换历史记录为菜谱格式
      recipes.value = response.data.map(history => ({
        id: history.recipe.id,
        name: history.recipe.name,
        cuisine: history.recipe.cuisineType,
        time: history.recipe.cookingTime,
        difficulty: history.recipe.difficultyLevel,
        description: history.recipe.description,
        servings: history.recipe.servings,
        // 映射食材字段名：quantity -> amount
        ingredients: (history.recipe.ingredients || []).map(ing => ({
          ingredientId: ing.ingredientId,  // 保留食材ID
          name: ing.name,
          amount: ing.quantity,  // 后端字段是 quantity，前端期望 amount
          available: true  // 默认为可用
        })),
        steps: history.recipe.steps || []
      }))
    }
  } catch (error) {
    console.error('加载菜谱失败:', error)
    // 如果加载失败，尝试从 localStorage 获取（向后兼容）
    const stored = localStorage.getItem('current-recipes')
    if (stored) {
      recipes.value = JSON.parse(stored)
    }
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
  // 评分和评论功能暂时禁用，等待后端API支持
  currentRecipe.value.rating = 0
  currentRecipe.value.comment = ''
  detailVisible.value = true
}

// 保存评分（暂时禁用）
const saveRating = () => {
  ElMessage.info('评分功能开发中，敬请期待')
}

// 保存评论（暂时禁用）
const saveComment = () => {
  ElMessage.info('评论功能开发中，敬请期待')
}

// 加入购物清单
const addToShopping = async (recipe) => {
  // 防止重复点击
  if (addingToCart.value.has(recipe.id)) {
    ElMessage.warning('正在添加中，请稍候...')
    return
  }

  addingToCart.value.add(recipe.id)

  try {
    let addedCount = 0
    let failedIngredients = []

    console.log(`开始添加菜谱 "${recipe.name}" 到购物清单，共 ${recipe.ingredients.length} 种食材`)

    for (const ing of recipe.ingredients) {
      try {
        // 优先使用食材ID（如果有的话），避免重复搜索
        let ingredientId = ing.ingredientId

        console.log(`处理食材: ${ing.name}, ingredientId: ${ingredientId}, amount: ${ing.amount}`)

        // 如果没有ingredientId，则通过名称搜索（向后兼容）
        if (!ingredientId) {
          console.log(`食材 ${ing.name} 没有ID，尝试搜索...`)
          const ingredientResponse = await searchIngredientByNameAPI(ing.name)
          console.log(`搜索结果:`, ingredientResponse)
          if (ingredientResponse.data) {
            ingredientId = ingredientResponse.data.id
            console.log(`找到食材ID: ${ingredientId}`)
          } else {
            console.warn(`搜索食材 ${ing.name} 失败，未找到匹配项`)
          }
        }

        if (ingredientId) {
          console.log(`添加食材 ${ing.name} (ID: ${ingredientId}) 到购物清单`)
          await addShoppingItemAPI({
            ingredientId: ingredientId,
            quantity: ing.amount,
            note: ''
          })
          addedCount++
          console.log(`成功添加食材 ${ing.name}`)
        } else {
          console.error(`食材 ${ing.name} 没有ID，无法添加`)
          failedIngredients.push(ing.name)
        }
      } catch (err) {
        console.error(`添加食材 ${ing.name} 失败:`, err)
        console.error('错误详情:', err.response || err.message || err)
        failedIngredients.push(ing.name)
      }
    }

    console.log(`添加完成: 成功 ${addedCount} 个，失败 ${failedIngredients.length} 个`)

    if (addedCount > 0) {
      if (failedIngredients.length > 0) {
        ElMessage.warning(`已添加 ${addedCount} 种食材，${failedIngredients.length} 种失败：${failedIngredients.join('、')}`)
      } else {
        ElMessage.success(`已添加 ${addedCount} 种食材到购物清单`)
      }
    }

    if (failedIngredients.length > 0 && addedCount === 0) {
      ElMessage.error(`所有食材添加失败：${failedIngredients.join('、')}。请查看控制台了解详情。`)
    }

    if (addedCount === 0 && failedIngredients.length === 0) {
      ElMessage.info('没有可添加的食材')
    }
  } catch (error) {
    console.error('添加到购物清单时发生错误:', error)
    ElMessage.error(error.message || '添加失败')
  } finally {
    // 完成后移除标记
    addingToCart.value.delete(recipe.id)
  }
}

// 删除菜谱
const deleteRecipe = async (recipe) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜谱"${recipe.name}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteRecipeAPI(recipe.id)

    // 从列表中移除
    recipes.value = recipes.value.filter(r => r.id !== recipe.id)

    // 如果删除的是当前查看的菜谱，关闭详情弹窗
    if (currentRecipe.value?.id === recipe.id) {
      detailVisible.value = false
      currentRecipe.value = null
    }

    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
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
${currentRecipe.value.steps.map((step, i) => `${i + 1}. ${step.description || step}`).join('\n')}
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
