<template>
  <div class="recipes-page">
    <div class="title-container">
      <img src="@/assets/images/厨师猫.png" alt="厨师猫" class="title-cat-left" />
      <h1 class="page-title">🍳 推荐菜谱</h1>
      <img src="@/assets/images/厨师猫.png" alt="厨师猫" class="title-cat-right" />
    </div>

    <el-container>
      <el-main>

        <div v-if="recipes.length === 0" class="empty-state">
          <img src="@/assets/images/困惑猫.png" alt="困惑猫" class="empty-cat-icon" />
          <div class="empty-text">还没有生成菜谱喵~</div>
          <el-button type="primary" @click="goToHome" class="empty-btn">去选择���材</el-button>
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
                <el-tag :type="getDifficultyType(recipe.difficulty)" class="difficulty-tag">
                  <img :src="getDifficultyIcon(recipe.difficulty)" :alt="getDifficultyLabel(recipe.difficulty)" class="difficulty-cat-icon" />
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
                <!-- 购物清单功能暂时隐藏 -->
                <!-- <el-button @click="addToShopping(recipe)">
                  <el-icon><ShoppingCart /></el-icon>
                  加入购物清单
                </el-button> -->
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
          <el-tag :type="getDifficultyType(currentRecipe.difficulty)" size="large" class="difficulty-tag">
            <img :src="getDifficultyIcon(currentRecipe.difficulty)" :alt="getDifficultyLabel(currentRecipe.difficulty)" class="difficulty-cat-icon-large" />
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

    <!-- 背景装饰猫爪 -->
    <div class="paw-decoration paw-1"></div>
    <div class="paw-decoration paw-2"></div>
    <div class="paw-decoration paw-3"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, Download, Delete } from '@element-plus/icons-vue'
import {
  addFavoriteAPI,
  removeFavoriteAPI,
  getFavoritesAPI,
  getHistoryAPI,
  deleteRecipeAPI
} from '../utils/api'

// 导入难度图标
import beginnerCatIcon from '@/assets/images/新手厨师猫.png'
import homeCookingCatIcon from '@/assets/images/普通厨师猫.png'
import chefCatIcon from '@/assets/images/专业厨师猫.png'

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

const getDifficultyIcon = (difficulty) => {
  const iconMap = {
    // 前端格式
    easy: beginnerCatIcon,
    medium: homeCookingCatIcon,
    hard: chefCatIcon,
    // 后端枚举格式
    BEGINNER: beginnerCatIcon,
    HOME_COOKING: homeCookingCatIcon,
    CHEF: chefCatIcon
  }
  return iconMap[difficulty] || homeCookingCatIcon
}
</script>

<style scoped>
.recipes-page {
  padding: 30px 20px;
  max-width: 1400px;
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

/* 空状态 */
.empty-state {
  padding: 80px 0;
  text-align: center;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.empty-cat-icon {
  width: 120px;
  height: 120px;
  object-fit: contain;
  margin-bottom: 20px;
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

.empty-text {
  font-size: 18px;
  color: #909399;
  margin-bottom: 24px;
}

.empty-btn {
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b9d 100%);
  border: none;
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 24px;
  transition: all 0.3s ease;
}

.empty-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 140, 158, 0.4);
}

/* 菜谱网格 */
.recipes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  animation: fadeIn 0.6s ease-out;
}

/* 菜谱卡片 */
.recipe-card {
  border-radius: 16px;
  border: 2px solid rgba(255, 140, 158, 0.15);
  overflow: hidden;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.recipe-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(255, 140, 158, 0.25);
  border-color: rgba(255, 140, 158, 0.3);
}

.recipe-card :deep(.el-card__header) {
  background: linear-gradient(135deg, rgba(255, 179, 120, 0.12) 0%, rgba(255, 140, 158, 0.12) 100%);
  border-bottom: 2px solid rgba(255, 140, 158, 0.2);
  padding: 18px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recipe-name {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  flex: 1;
}

.recipe-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px;
}

/* 标签 */
.recipe-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.recipe-tags :deep(.el-tag) {
  border-radius: 12px;
  padding: 6px 14px;
  font-weight: 500;
}

.difficulty-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.difficulty-cat-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.difficulty-cat-icon-large {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

/* 食材预览 */
.ingredients-preview h4 {
  margin-bottom: 12px;
  color: #606266;
  font-size: 15px;
  font-weight: 600;
}

.ingredient-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  padding: 4px 0;
}

.available {
  color: #67c23a;
  font-size: 16px;
}

.missing {
  color: #f56c6c;
  font-size: 16px;
}

.more-hint {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
  font-style: italic;
}

/* 操作按钮 */
.recipe-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
}

.recipe-actions .el-button {
  flex: 1;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.recipe-actions .el-button--primary {
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b9d 100%);
  border: none;
}

.recipe-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 140, 158, 0.4);
}

.recipe-actions .el-button--danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
}

/* 详情弹窗样式 */
.recipe-dialog :deep(.el-dialog) {
  border-radius: 16px;
}

.recipe-detail {
  max-height: 70vh;
  overflow-y: auto;
  padding: 4px;
}

.recipe-detail::-webkit-scrollbar {
  width: 6px;
}

.recipe-detail::-webkit-scrollbar-thumb {
  background: rgba(255, 140, 158, 0.3);
  border-radius: 3px;
}

.recipe-detail::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 140, 158, 0.5);
}

.detail-tags {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.detail-tags :deep(.el-tag) {
  border-radius: 12px;
  padding: 8px 16px;
  font-weight: 500;
}

.detail-section {
  margin-bottom: 32px;
}

.detail-section h3 {
  font-size: 20px;
  margin-bottom: 16px;
  color: #303133;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-section :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

.detail-section :deep(.el-steps) {
  padding: 12px;
  background: rgba(255, 179, 120, 0.05);
  border-radius: 12px;
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

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(var(--rotation, 0deg));
  }
  50% {
    transform: translateY(-20px) rotate(var(--rotation, 0deg));
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .recipes-page {
    padding: 20px 15px;
  }

  .page-title {
    font-size: 32px;
  }

  .title-cat-left,
  .title-cat-right {
    width: 45px;
    height: 45px;
  }

  .recipes-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .recipe-actions {
    flex-direction: column;
  }

  .recipe-actions .el-button {
    width: 100%;
  }
}
</style>
