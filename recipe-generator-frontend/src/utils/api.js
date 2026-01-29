import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加 token 到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const message = error.response?.data?.message || error.message || '请求失败'

    // 如果是 401 未授权，清除 token 并跳转到登录页
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.dispatchEvent(new CustomEvent('navigate', { detail: 'login' }))
    }

    return Promise.reject(new Error(message))
  }
)

// 用户认证相关 API
export const loginAPI = async (data) => {
  return await api.post('/auth/login', data)
}

export const registerAPI = async (data) => {
  return await api.post('/auth/register', data)
}

export const getUserInfoAPI = async () => {
  return await api.get('/user/info')
}

// 生成食谱
export const generateRecipesAPI = async (params) => {
  return await api.post('/recipes/generate', params)
}

// 获取食谱详情
export const getRecipeDetail = async (id) => {
  return await api.get(`/recipes/${id}`)
}

export default api
