<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 左侧装饰区 -->
      <div class="register-decoration">
        <div class="decoration-content">
          <h1 class="brand-title">🍳 加入我们</h1>
          <p class="brand-slogan">开启你的智能烹饪之旅</p>
          <div class="benefit-list">
            <div class="benefit-item">
              <span class="benefit-icon">🎯</span>
              <div class="benefit-text">
                <h3>个性化推荐</h3>
                <p>根据你的口味偏好推荐菜谱</p>
              </div>
            </div>
            <div class="benefit-item">
              <span class="benefit-icon">💾</span>
              <div class="benefit-text">
                <h3>云端同步</h3>
                <p>收藏和历史记录随时随地访问</p>
              </div>
            </div>
            <div class="benefit-item">
              <span class="benefit-icon">🤖</span>
              <div class="benefit-text">
                <h3>AI 智能生成</h3>
                <p>基于 Claude AI 的专业菜谱</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="register-form-wrapper">
        <el-card class="register-card" shadow="always">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>填写信息，开始你的美食探索</p>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            size="large"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名（3-20个字符）"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（6-20个字符）"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                style="width: 100%"
                @click="handleRegister"
              >
                <span v-if="!loading">🎉 注册</span>
                <span v-else>注册中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>已有账号？</span>
            <el-button type="primary" link @click="goToLogin">
              立即登录
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
import { registerAPI } from '../utils/api'

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await registerAPI({
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password
      })

      ElMessage.success('注册成功！请登录')

      // 跳转到登录页
      setTimeout(() => {
        window.dispatchEvent(new CustomEvent('navigate', { detail: 'login' }))
      }, 1000)
    } catch (error) {
      ElMessage.error(error.message || '注册失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  window.dispatchEvent(new CustomEvent('navigate', { detail: 'login' }))
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  display: flex;
  max-width: 1100px;
  width: 100%;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.register-decoration {
  flex: 1;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.decoration-content {
  max-width: 450px;
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

.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.benefit-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(10px);
}

.benefit-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.benefit-text h3 {
  font-size: 18px;
  margin-bottom: 5px;
  font-weight: 600;
}

.benefit-text p {
  font-size: 14px;
  opacity: 0.9;
  line-height: 1.5;
}

.register-form-wrapper {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-card {
  width: 100%;
  max-width: 450px;
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
  .register-container {
    flex-direction: column;
  }

  .register-decoration {
    padding: 40px 20px;
  }

  .brand-title {
    font-size: 28px;
  }

  .register-form-wrapper {
    padding: 40px 20px;
  }
}
</style>
