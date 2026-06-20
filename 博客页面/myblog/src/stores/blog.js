import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useBlogStore = defineStore('blog', () => {
  // 状态
  const articles = ref([])
  const categories = ref([])
  const loading = ref(false)

  // 计算属性
  const featuredArticles = computed(() => {
    return articles.value.slice(0, 3)
  })

  const getArticlesByCategory = computed(() => {
    return (categoryId) => {
      return articles.value.filter(article => article.categoryId === categoryId)
    }
  })

  // 动作
  const fetchArticles = async () => {
    loading.value = true
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      articles.value = [
        {
          id: 1,
          title: 'Vue3 组合式API最佳实践',
          excerpt: '深入探讨Vue3组合式API的使用技巧和最佳实践...',
          content: '这里是文章的完整内容...',
          cover: '/api/images/vue3.jpg',
          publishDate: '2024-01-15',
          readTime: '8分钟',
          category: '前端开发',
          categoryId: 1,
          tags: ['Vue3', 'JavaScript', '前端']
        },
        {
          id: 2,
          title: '现代CSS布局技术详解',
          excerpt: '从Flexbox到Grid，全面了解现代CSS布局技术...',
          content: '这里是文章的完整内容...',
          cover: '/api/images/css.jpg',
          publishDate: '2024-01-10',
          readTime: '12分钟',
          category: 'CSS',
          categoryId: 1,
          tags: ['CSS', '布局', '响应式']
        }
      ]
    } catch (error) {
      console.error('Failed to fetch articles:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchCategories = async () => {
    try {
      categories.value = [
        { id: 1, name: '前端开发', count: 15 },
        { id: 2, name: '后端技术', count: 8 },
        { id: 3, name: '移动开发', count: 6 },
        { id: 4, name: '设计思考', count: 4 }
      ]
    } catch (error) {
      console.error('Failed to fetch categories:', error)
    }
  }

  const getArticleById = (id) => {
    return articles.value.find(article => article.id === id)
  }

  return {
    // 状态
    articles,
    categories,
    loading,
    
    // 计算属性
    featuredArticles,
    getArticlesByCategory,
    
    // 动作
    fetchArticles,
    fetchCategories,
    getArticleById
  }
}) 