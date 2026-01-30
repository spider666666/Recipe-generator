<template>
  <div class="shopping-page">
    <el-container>
      <el-main>
        <div class="header">
          <h1 class="page-title">购物清单</h1>
          <div class="header-actions">
            <el-button type="primary" @click="exportList">
              <el-icon><Download /></el-icon>
              导出清单
            </el-button>
            <el-button type="danger" @click="clearList">
              <el-icon><Delete /></el-icon>
              清空清单
            </el-button>
          </div>
        </div>

        <div v-if="shoppingList.length === 0" class="empty-state">
          <el-empty description="购物清单是空的">
            <el-button type="primary" @click="goToRecipes">去查看菜谱</el-button>
          </el-empty>
        </div>

        <div v-else class="shopping-content">
          <!-- 统计信息 -->
          <el-card class="stats-card">
            <div class="stats">
              <div class="stat-item">
                <span class="stat-label">总计：</span>
                <span class="stat-value">{{ shoppingList.length }} 种</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">已购买：</span>
                <span class="stat-value">{{ checkedCount }} 种</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">待购买：</span>
                <span class="stat-value">{{ uncheckedCount }} 种</span>
              </div>
            </div>
          </el-card>

          <!-- 按分类展示 -->
          <div class="category-sections">
            <el-card
              v-for="(items, category) in groupedList"
              :key="category"
              class="category-card"
            >
              <template #header>
                <div class="category-header">
                  <span class="category-name">{{ getCategoryIcon(category) }} {{ category }}</span>
                  <span class="category-count">{{ items.length }} 种</span>
                </div>
              </template>

              <div class="shopping-items">
                <div
                  v-for="item in items"
                  :key="item.id"
                  class="shopping-item"
                  :class="{ checked: item.checked }"
                >
                  <el-checkbox
                    v-model="item.checked"
                    @change="togglePurchaseStatus(item)"
                    size="large"
                  />

                  <div class="item-info">
                    <div class="item-name">{{ item.name }}</div>
                    <el-input
                      v-model="item.amount"
                      size="small"
                      placeholder="数量"
                      style="width: 120px"
                      @blur="saveList"
                    />
                  </div>

                  <el-input
                    v-model="item.note"
                    placeholder="备注"
                    size="small"
                    style="flex: 1; margin: 0 12px"
                    @blur="saveList"
                  >
                    <template #prefix>
                      <el-icon><EditPen /></el-icon>
                    </template>
                  </el-input>

                  <el-button
                    :icon="Delete"
                    type="danger"
                    circle
                    size="small"
                    @click="removeItem(item.id)"
                  />
                </div>
              </div>
            </el-card>
          </div>

          <!-- 添加新项 -->
          <el-card class="add-card">
            <template #header>
              <span>添加新项目</span>
            </template>
            <el-row :gutter="12">
              <el-col :span="6">
                <el-input v-model="newItem.name" placeholder="食材名称" />
              </el-col>
              <el-col :span="5">
                <el-input v-model="newItem.amount" placeholder="数量" />
              </el-col>
              <el-col :span="5">
                <el-select v-model="newItem.category" placeholder="分类" style="width: 100%">
                  <el-option label="蔬菜类" value="蔬菜类" />
                  <el-option label="肉类" value="肉类" />
                  <el-option label="海鲜类" value="海鲜类" />
                  <el-option label="主食类" value="主食类" />
                  <el-option label="调味料" value="调味料" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-col>
              <el-col :span="6">
                <el-input v-model="newItem.note" placeholder="备注" />
              </el-col>
              <el-col :span="2">
                <el-button type="primary" @click="addItem" style="width: 100%">
                  添加
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete, EditPen } from '@element-plus/icons-vue'
import {
  getShoppingListAPI,
  updatePurchaseStatusAPI,
  deleteShoppingItemAPI,
  clearShoppingListAPI
} from '../utils/api'

const shoppingList = ref([])
const newItem = ref({
  name: '',
  amount: '',
  category: '其他',
  note: ''
})

onMounted(() => {
  loadList()
  // 监听导航事件，当切换到购物清单页面时重新加载
  window.addEventListener('navigate', handleNavigate)
})

const handleNavigate = (event) => {
  if (event.detail === 'shopping') {
    loadList()
  }
}

const loadList = async () => {
  try {
    const response = await getShoppingListAPI()
    if (response.data) {
      // 转换后端数据格式为前端格式
      shoppingList.value = response.data.map(item => ({
        id: item.id,
        name: item.ingredient?.name || '未知食材',
        amount: item.quantity,
        category: getCategoryDisplayName(item.ingredient?.category) || '其他',
        note: item.note || '',
        checked: item.isPurchased || false,
        ingredientId: item.ingredientId
      }))
    }
  } catch (error) {
    console.error('加载购物清单失败:', error)
    ElMessage.error('加载购物清单失败')
  }
}

// 将后端枚举值转换为中文显示名称
const getCategoryDisplayName = (category) => {
  const categoryMap = {
    'VEGETABLE': '蔬菜类',
    'MEAT': '肉类',
    'SEAFOOD': '海鲜类',
    'STAPLE': '主食类',
    'SEASONING': '调味料',
    'OTHER': '其他'
  }
  return categoryMap[category] || '其他'
}

const saveList = async () => {
  // 由于后端API不支持批量更新，这里暂时不做处理
  // 实际的更新会在 togglePurchaseStatus 和 removeItem 中进行
}

// 切换购买状态
const togglePurchaseStatus = async (item) => {
  try {
    await updatePurchaseStatusAPI(item.id, item.checked)
  } catch (error) {
    console.error('更新状态失败:', error)
    // 恢复原状态
    item.checked = !item.checked
    ElMessage.error('更新状态失败')
  }
}

// 统计
const checkedCount = computed(() => {
  return shoppingList.value.filter(item => item.checked).length
})

const uncheckedCount = computed(() => {
  return shoppingList.value.filter(item => !item.checked).length
})

// 按分类分组
const groupedList = computed(() => {
  const groups = {}
  shoppingList.value.forEach(item => {
    const category = item.category || '其他'
    if (!groups[category]) {
      groups[category] = []
    }
    groups[category].push(item)
  })
  return groups
})

// 添加项目
const addItem = async () => {
  if (!newItem.value.name) {
    ElMessage.warning('请输入食材名称')
    return
  }

  // 注意：这个功能需要先查找或创建食材，暂时禁用
  ElMessage.warning('请从菜谱页面添加食材到购物清单')

  // 重置表单
  newItem.value = {
    name: '',
    amount: '',
    category: '其他',
    note: ''
  }
}

// 移除项目
const removeItem = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个项目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteShoppingItemAPI(id)
    await loadList()
    ElMessage.success('已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 清空清单
const clearList = async () => {
  if (shoppingList.value.length === 0) {
    ElMessage.warning('清单已经是空的')
    return
  }

  try {
    await ElMessageBox.confirm('确定要清空整个购物清单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await clearShoppingListAPI()
    await loadList()
    ElMessage.success('已清空')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

// 导出清单
const exportList = () => {
  if (shoppingList.value.length === 0) {
    ElMessage.warning('清单是空的')
    return
  }

  const content = `
购物清单
导出时间：${new Date().toLocaleString('zh-CN')}

${Object.entries(groupedList.value).map(([category, items]) => `
【${category}】
${items.map((item, i) => `${i + 1}. ${item.checked ? '✅' : '⬜'} ${item.name} ${item.amount}${item.note ? ` (${item.note})` : ''}`).join('\n')}
`).join('\n')}

统计：
- 总计：${shoppingList.value.length} 种
- 已购买：${checkedCount.value} 种
- 待购买：${uncheckedCount.value} 种
  `.trim()

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `购物清单_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.txt`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}

const goToRecipes = () => {
  window.dispatchEvent(new CustomEvent('navigate', { detail: 'recipes' }))
}

const getCategoryIcon = (category) => {
  const icons = {
    '蔬菜类': '🥬',
    '肉类': '🥩',
    '海鲜类': '🦐',
    '主食类': '🍚',
    '调味料': '🧂',
    '其他': '📦'
  }
  return icons[category] || '📦'
}
</script>

<style scoped>
.shopping-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.empty-state {
  padding: 60px 0;
}

.shopping-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-card .stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.category-sections {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-name {
  font-size: 18px;
  font-weight: bold;
}

.category-count {
  font-size: 14px;
  color: #909399;
}

.shopping-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shopping-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.3s;
}

.shopping-item:hover {
  background: #f5f7fa;
  border-color: #409eff;
}

.shopping-item.checked {
  opacity: 0.6;
}

.shopping-item.checked .item-name {
  text-decoration: line-through;
  color: #909399;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 150px;
}

.item-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.add-card {
  border: 2px dashed #dcdfe6;
}

.add-card:hover {
  border-color: #409eff;
}
</style>
