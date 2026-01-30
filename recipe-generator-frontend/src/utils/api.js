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
  return await api.get('/user/current')
}

// 生成食谱
export const generateRecipesAPI = async (params) => {
  return await api.post('/recipes/generate', params)
}

// 获取食谱详情
export const getRecipeDetail = async (id) => {
  return await api.get(`/recipes/${id}`)
}

// 删除食谱
export const deleteRecipeAPI = async (id) => {
  return await api.delete(`/recipes/${id}`)
}

// 食材相关 API
export const searchIngredientByNameAPI = async (name) => {
  return await api.get('/ingredients/search', { params: { name } })
}

export const getAllIngredientsAPI = async () => {
  return await api.get('/ingredients')
}

// 收藏相关 API
export const addFavoriteAPI = async (recipeId) => {
  return await api.post(`/favorites/${recipeId}`)
}

export const removeFavoriteAPI = async (recipeId) => {
  return await api.delete(`/favorites/${recipeId}`)
}

export const getFavoritesAPI = async () => {
  return await api.get('/favorites')
}

export const checkFavoriteAPI = async (recipeId) => {
  return await api.get(`/favorites/check/${recipeId}`)
}

// 历史记录相关 API
export const addHistoryAPI = async (recipeId) => {
  return await api.post(`/history/${recipeId}`)
}

export const getHistoryAPI = async () => {
  return await api.get('/history')
}

export const deleteHistoryAPI = async (historyId) => {
  return await api.delete(`/history/${historyId}`)
}

export const clearHistoryAPI = async () => {
  return await api.delete('/history')
}

// 购物清单相关 API
export const addShoppingItemAPI = async (data) => {
  return await api.post('/shopping-list', data)
}

export const getShoppingListAPI = async () => {
  return await api.get('/shopping-list')
}

export const updatePurchaseStatusAPI = async (itemId, isPurchased) => {
  return await api.put(`/shopping-list/${itemId}/purchase`, null, {
    params: { isPurchased }
  })
}

export const deleteShoppingItemAPI = async (itemId) => {
  return await api.delete(`/shopping-list/${itemId}`)
}

export const clearShoppingListAPI = async () => {
  return await api.delete('/shopping-list')
}

// 食材组合相关 API
export const saveCombosAPI = async (data) => {
  return await api.post('/combos', data)
}

export const getCombosAPI = async () => {
  return await api.get('/combos')
}

export const deleteComboAPI = async (comboId) => {
  return await api.delete(`/combos/${comboId}`)
}

export const updateComboAPI = async (comboId, data) => {
  return await api.put(`/combos/${comboId}`, data)
}

export default api
