// Token验证和管理工具函数
import request from './request.js'

// 缓存token验证结果，避免频繁调用后端接口
let tokenValidationCache = {
  isValid: null,
  timestamp: 0,
  cacheDuration: 30000 // 缓存30秒
}

/**
 * 检查token是否存在
 * @returns {boolean}
 */
export function hasToken() {
  return !!localStorage.getItem('token')
}

/**
 * 获取token
 * @returns {string|null}
 */
export function getToken() {
  return localStorage.getItem('token')
}

/**
 * 设置token
 * @param {string} token
 */
export function setToken(token) {
  localStorage.setItem('token', token)
  // 清除缓存，因为token已更新
  tokenValidationCache.isValid = null
}

/**
 * 清除token
 */
export function removeToken() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  // 清除缓存
  tokenValidationCache.isValid = null
}

/**
 * 验证token是否有效（通过调用后端验证接口）
 * @returns {Promise<boolean>}
 */
export async function validateToken() {
  const token = getToken()
  
  // 如果没有token，直接返回false
  if (!token) {
    tokenValidationCache.isValid = false
    return false
  }
  
  // 检查缓存是否有效
  const now = Date.now()
  if (tokenValidationCache.isValid !== null && 
      (now - tokenValidationCache.timestamp) < tokenValidationCache.cacheDuration) {
    return tokenValidationCache.isValid
  }
  
  try {
    // 调用后端验证接口
    const response = await request.get('/validate')
    const isValid = response && response.isValid === true
    
    // 更新缓存
    tokenValidationCache.isValid = isValid
    tokenValidationCache.timestamp = now
    
    return isValid
  } catch (error) {
    console.error('[validateToken] Token验证失败:', error)
    
    // 如果是401错误，说明token已过期
    if (error.response && error.response.status === 401) {
      tokenValidationCache.isValid = false
      tokenValidationCache.timestamp = now
      return false
    }
    
    // 其他错误，暂时认为token有效（避免网络问题导致用户被登出）
    return true
  }
}

/**
 * 验证token是否有效（通过检查是否存在）
 * @returns {boolean}
 */
export function isTokenValid() {
  const token = getToken()
  return !!token
}

/**
 * 检查用户是否已登录（同步版本，只检查token是否存在）
 * @returns {boolean}
 */
export function isLoggedIn() {
  return isTokenValid()
}

/**
 * 检查用户是否已登录（异步版本，真正验证token有效性）
 * @returns {Promise<boolean>}
 */
export async function isLoggedInAsync() {
  return await validateToken()
}

/**
 * 登出操作
 */
export function logout() {
  removeToken()
}

// 全局 token 定时同步器
let tokenRefreshTimer = null

/**
 * 启动定时器，用于定期执行回调（例如刷新登录态）
 * @param {Function} callback 执行的回调
 * @param {number} interval 间隔毫秒，默认 5 分钟
 */
export function startTokenAutoRefresh(callback = () => {
}, interval = 5 * 60 * 1000) {
    stopTokenAutoRefresh()
    tokenRefreshTimer = setInterval(() => {
        try {
            callback()
        } catch (e) {
            console.error('[startTokenAutoRefresh]', e)
        }
    }, interval)
}

/**
 * 停止定时器
 */
export function stopTokenAutoRefresh() {
    if (tokenRefreshTimer) {
        clearInterval(tokenRefreshTimer)
        tokenRefreshTimer = null
    }
}