<template>
  <div id="app">
    <!-- 导航栏 -->
    <el-menu
      v-if="activeMenu !== 'login' && activeMenu !== 'register'"
      :default-active="activeMenu"
      mode="horizontal"
      @select="handleMenuSelect"
      class="app-menu"
    >
      <div class="logo">🍳 智能食谱生成器</div>
      <el-menu-item index="home">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="recipes">
        <el-icon><Reading /></el-icon>
        <span>菜谱</span>
      </el-menu-item>
      <el-menu-item index="favorites">
        <el-icon><Star /></el-icon>
        <span>收藏夹</span>
      </el-menu-item>
      <el-menu-item index="shopping">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物清单</span>
      </el-menu-item>

      <!-- 用户信息区域 -->
      <div class="user-section">
        <template v-if="isLoggedIn">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ userInfo.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" link @click="activeMenu = 'login'">
            登录
          </el-button>
          <el-button type="primary" @click="activeMenu = 'register'">
            注册
          </el-button>
        </template>
      </div>
    </el-menu>

    <!-- 主内容区 -->
    <div class="main-content">
      <component :is="currentView" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, markRaw, onMounted, onUnmounted } from 'vue'
import { HomeFilled, Reading, Star, ShoppingCart, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import HomePage from './views/HomePage.vue'
import RecipesPage from './views/RecipesPage.vue'
import FavoritesPage from './views/FavoritesPage.vue'
import ShoppingPage from './views/ShoppingPage.vue'
import LoginPage from './views/LoginPage.vue'
import RegisterPage from './views/RegisterPage.vue'

const activeMenu = ref('home')
const userInfo = ref(null)

const views = {
  home: markRaw(HomePage),
  recipes: markRaw(RecipesPage),
  favorites: markRaw(FavoritesPage),
  shopping: markRaw(ShoppingPage),
  login: markRaw(LoginPage),
  register: markRaw(RegisterPage)
}

const currentView = computed(() => views[activeMenu.value])

// 检查登录状态
const isLoggedIn = computed(() => !!userInfo.value)

// 加载用户信息
const loadUserInfo = () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    try {
      userInfo.value = JSON.parse(storedUserInfo)
    } catch (e) {
      console.error('Failed to parse user info:', e)
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
    }
  }
}

const handleMenuSelect = (index) => {
  // 如果未登录且访问需要登录的页面，跳转到登录页
  if (!isLoggedIn.value && ['recipes', 'favorites', 'shopping'].includes(index)) {
    ElMessage.warning('请先登录')
    activeMenu.value = 'login'
    return
  }
  activeMenu.value = index
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    userInfo.value = null
    activeMenu.value = 'login'
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消
  }
}

// 处理用户下拉菜单命令
const handleUserCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

// 监听自定义导航事件
const handleNavigate = (event) => {
  activeMenu.value = event.detail
}

// 监听登录成功事件
const handleLoginSuccess = () => {
  loadUserInfo()
}

onMounted(() => {
  loadUserInfo()

  // 如果没有登录，默认显示登录页
  if (!isLoggedIn.value) {
    activeMenu.value = 'login'
  }

  window.addEventListener('navigate', handleNavigate)
  window.addEventListener('login-success', handleLoginSuccess)
})

onUnmounted(() => {
  window.removeEventListener('navigate', handleNavigate)
  window.removeEventListener('login-success', handleLoginSuccess)
})
</script>

<style scoped>
.app-menu {
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  padding: 0 20px;
  line-height: 60px;
  color: #409eff;
}

.user-section {
  margin-left: auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-info span {
  color: #303133;
  font-size: 14px;
}

.main-content {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
}
</style>
