<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧装饰区 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <h1 class="brand-title">🍳 智能食谱生成器</h1>
          <p class="brand-slogan">用 AI 发现美食的无限可能</p>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">🥗</span>
              <span>智能食材搭配</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📖</span>
              <span>详细烹饪步骤</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⭐</span>
              <span>收藏喜爱菜谱</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🛒</span>
              <span>便捷购物清单</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <el-card class="login-card" shadow="always">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录开始你的美食之旅</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            size="large"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                style="width: 100%"
                @click="handleLogin"
              >
                <span v-if="!loading">🍴 登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>还没有账号？</span>
            <el-button type="primary" link @click="goToRegister">
              立即注册
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { loginAPI } from '../utils/api'

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const response = await loginAPI(loginForm)

      // 保存 token 和用户信息
      // 后端返回格式: { code, message, data: { accessToken, username } }
      localStorage.setItem('token', response.data.accessToken)
      localStorage.setItem('userInfo', JSON.stringify({
        username: response.data.username
      }))

      ElMessage.success('登录成功！')

      // 跳转到首页
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent('navigate', { detail: 'home' }))
        window.dispatchEvent(new CustomEvent('login-success'))
      }, 500)
    } catch (error) {
      ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => {
  window.dispatchEvent(new CustomEvent('navigate', { detail: 'register' }))
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  display: flex;
  max-width: 1000px;
  width: 100%;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-decoration {
  flex: 1;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.decoration-content {
  max-width: 400px;
}

.brand-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.brand-slogan {
  font-size: 18px;
  margin-bottom: 40px;
  opacity: 0.95;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 16px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(10px);
}

.feature-icon {
  font-size: 28px;
}

.login-form-wrapper {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border: none;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.form-header p {
  color: #909399;
  font-size: 14px;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .login-decoration {
    padding: 40px 20px;
  }

  .brand-title {
    font-size: 28px;
  }

  .login-form-wrapper {
    padding: 40px 20px;
  }
}
</style>
