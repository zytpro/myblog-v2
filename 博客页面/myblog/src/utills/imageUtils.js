import { IMAGE_BASE_URL, MUSIC_BASE_URL } from '../config/env.js';

/**
 * 图片URL处理工具函数
 */

/**
 * 解析图片URL，确保在开发和生产环境都能正确显示
 * @param {string} imagePath - 图片路径
 * @param {string} fallback - 默认图片路径
 * @returns {string} 完整的图片URL
 */
export const resolveImageUrl = (imagePath, fallback = '') => {
  if (!imagePath) return fallback
  
  // 如果是完整的URL（http/https/data），直接返回
  if (/^(data:|https?:\/\/)/i.test(imagePath)) {
    return imagePath
  }
  
  // 使用环境配置中的图片基础URL
  const cleanBase = IMAGE_BASE_URL.replace(/\/$/, '')
  const cleanPath = imagePath.replace(/^\//, '')
  
  return `${cleanBase}/${cleanPath}`
}

/**
 * 解析音乐文件URL
 * @param {string} fileName - 文件名
 * @param {string} type - 类型：'preview' 或 'cover'
 * @returns {string} 完整的音乐文件URL
 */
export const resolveMusicUrl = (fileName, type = 'preview') => {
  if (!fileName) return ''
  
  const cleanBase = MUSIC_BASE_URL.replace(/\/$/, '')
  
  if (type === 'cover') {
    return `${cleanBase}/file/cover/music/${fileName}`
  } else {
    return `${cleanBase}/file/preview/music/${fileName}`
  }
}

/**
 * 检查图片是否可访问
 * @param {string} url - 图片URL
 * @returns {Promise<boolean>} 是否可访问
 */
export const checkImageAccessible = (url) => {
  return new Promise((resolve) => {
    if (!url) {
      resolve(false)
      return
    }
    
    const img = new Image()
    img.onload = () => resolve(true)
    img.onerror = () => resolve(false)
    img.src = url
  })
}

/**
 * 获取图片加载状态
 * @param {string} url - 图片URL
 * @param {string} fallback - 默认图片
 * @returns {Promise<{success: boolean, url: string}>} 加载结果
 */
export const getImageLoadResult = async (url, fallback = '') => {
  if (!url) {
    return { success: false, url: fallback }
  }
  
  try {
    const isAccessible = await checkImageAccessible(url)
    return {
      success: isAccessible,
      url: isAccessible ? url : fallback
    }
  } catch (error) {
    return { success: false, url: fallback }
  }
} 