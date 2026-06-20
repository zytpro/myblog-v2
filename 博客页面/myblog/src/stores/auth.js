import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { isLoggedIn as checkLoginStatus, getToken, isLoggedInAsync } from '../utills/auth.js'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const isAuthenticated = ref(checkLoginStatus())
  const isTokenValidating = ref(false)
  
  // 计算属性
  const loggedIn = computed(() => isAuthenticated.value)
  
  // 方法
  const updateLoginStatus = () => {
    isAuthenticated.value = checkLoginStatus()
  }
  
  // 异步验证登录状态
  const validateLoginStatus = async () => {
    isTokenValidating.value = true
    try {
      const isValid = await isLoggedInAsync()
      isAuthenticated.value = isValid
      return isValid
    } catch (error) {
      console.error('[validateLoginStatus] 验证失败:', error)
      return false
    } finally {
      isTokenValidating.value = false
    }
  }
  
  const setLoggedIn = (status) => {
    isAuthenticated.value = status
  }
  
  const logout = () => {
    isAuthenticated.value = false
  }
  
  return {
    isAuthenticated,
    isTokenValidating,
    loggedIn,
    updateLoginStatus,
    validateLoginStatus,
    setLoggedIn,
    logout
  }
}) 