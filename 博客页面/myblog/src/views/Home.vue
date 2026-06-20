<template>
  <div class="jiujiu-home-page">
    <div class="jiujiu-background">
      <img
          class="background-image-index"
          :src="backgroundImage"
          alt="bg"
          @error="onImageError"
          @load="onImageLoad"
      />

      <div class="signature-wall myCenter my-animation-hideToShow">
        <h1 class="playful">
          <span v-for="(char, index) in webTitle" :key="index">{{ char }}</span>
        </h1>
        <div class="printer" @click="getGuShi()">
          <div class="printer-content">
            <h3>
              {{ displayText }}<span class="cursor">|</span>
            </h3>
          </div>
        </div>
        <i class="el-icon-arrow-down" @click="navigation('.page-container-wrap')">
          <svg viewBox="0 0 1024 1024" width="30" height="30" fill="currentColor">
            <path
                d="M831.872 340.864 512 652.672 192.128 340.864a30.592 30.592 0 0 0-42.752 0 29.12 29.12 0 0 0 0 41.6L489.664 714.24a32 32 0 0 0 44.672 0l340.288-331.712a29.12 29.12 0 0 0 0-41.728 30.592 30.592 0 0 0-42.752 0z"></path>
          </svg>
        </i>
      </div>

      <div id="bannerWave1"></div>
      <div id="bannerWave2"></div>
    </div>

    <MusicPlayer/>

    <div class="page-container-wrap">
      <div class="page-container">
        <div class="aside-content" v-if="showAside">
          <div class="jiujiu-aside-card">


            <!-- 个人信息卡片 -->
            <div class="profile-card">
              <div class="profile-avatar-wrap">
                <el-avatar
                    :size="100"
                    :src="userInfo.avatar || '/icon/001.png'"
                    class="user-avatar"
                >
                  <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
                </el-avatar>
              </div>

              <div class="profile-info">
                <h3 class="profile-name">{{ userInfo.username || 'jiujiu' }}</h3>
                <p class="profile-signature">{{ userInfo.description || '万物皆有裂痕，那是光照进来的地方' }}</p>
              </div>

              <div class="profile-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ articlesForCards.length }}</span>
                  <span class="stat-label">ARTICLES</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ categories.length }}</span>
                  <span class="stat-label">CATEGORIES</span>
                </div>
              </div>
            </div>


            <div class="social-links">
              <a href="#" class="social-link qq-link" title="QQ">
                <el-icon size="20">
                  <ChatDotRound/>
                </el-icon>
              </a>
              <a href="#" class="social-link douyin-link" title="抖音">
                <el-icon size="20">
                  <VideoPlay/>
                </el-icon>
              </a>
              <a href="#" class="social-link weibo-link" title="微博">
                <el-icon size="20">
                  <Share/>
                </el-icon>
              </a>
              <a href="#" class="social-link github-link" title="GitHub">
                <el-icon size="20">
                  <Link/>
                </el-icon>
              </a>
            </div>
          </div>
        </div>

        <div class="recent-posts">
          <div class="announcement background-opacity">
            <div class="announcement-icon">
              <svg viewBox="0 0 1024 1024" width="20" height="20" fill="currentColor">
                <path
                    d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"></path>
                <path
                    d="M464 336a48 48 0 1 0 96 0 48 48 0 1 0-96 0zm72 112h-48c-4.4 0-8 3.6-8 8v272c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8V456c0-4.4-3.6-8-8-8z"></path>
              </svg>
            </div>
            <div class="announcement-content-wrap">
              <div v-for="(announcement, index) in announcements" :key="index">
                {{ announcement.content }}
              </div>
              <div v-if="announcements.length === 0">暂无公告</div>
            </div>
          </div>

          <div v-if="articlesLoading" class="articles-loading">
            <div v-for="n in 3" :key="'skeleton-' + n" class="article-skeleton">
              <div class="skeleton-image"></div>
              <div class="skeleton-content">
                <div class="skeleton-title"></div>
                <div class="skeleton-description"></div>
                <div class="skeleton-meta"></div>
              </div>
            </div>
          </div>

          <div v-else-if="articlesError" class="articles-error">
            <p>加载文章失败：{{ articlesError }}</p>
          </div>

          <div v-else-if="!articlesForCards.length" class="articles-empty">
            <p>暂无文章，敬请期待～</p>
          </div>

          <div v-else class="jiujiu-articles-list">
            <ArticleCard
              v-for="(a, idx) in articlesForCards"
              :key="a.id"
              :article="a"
              :index="idx"
              class="fade-in-up"
              :style="{ animationDelay: `${idx * 0.1}s` }"
            />
          </div>

          <div class="pagination-wrap">
            <div @click="loadMoreArticles" class="pagination" v-if="hasMoreArticles">
              加载更多
            </div>
            <div v-else style="user-select: none; color: #999; margin-top: 20px;">
              ~~到底啦~~
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBlogStore } from '../stores/blog'
import ArticleCard from '../components/ArticleCard.vue'
import Carousel from '../components/Carousel.vue'
import MusicPlayer from '../components/MusicPlayer.vue'
import request from '../utills/request.js'
import { 
  ChatDotRound, 
  VideoPlay, 
  Share, 
  Link 
} from '@element-plus/icons-vue'

const router = useRouter()
const blogStore = useBlogStore()

const showAside = ref(true)
const webTitle = ref('我的博客')
const displayText = ref('')
const currentTextIndex = ref(0)
const isTyping = ref(false)
const isDeleting = ref(false)

const typewriterTexts = ref([
  '你看对面的青山多漂亮',
  '春风得意马蹄疾，一日看尽长安花',
  '山重水复疑无路，柳暗花明又一村',
  '会当凌绝顶，一览众山小',
  '海上生明月，天涯共此时',
  '落红不是无情物，化作春泥更护花',
  '长风破浪会有时，直挂云帆济沧海',
  '天生我材必有用，千金散尽还复来'
])

const backgroundImage = ref('/img/back1.png')
const imageLoadAttempts = ref(0)
const maxImageLoadAttempts = 3
const fallbackImages = [
  '/back.webp',
  'https://picsum.photos/1920/1080?random=1'
]

const articles = ref([])
const articlesLoading = ref(false)
const articlesError = ref('')
const hasMoreArticles = ref(true)

const articlesCache = ref({data: [], timestamp: 0, version: '1.0'})
const cacheExpiryTime = 5 * 60 * 1000
const isRefreshing = ref(false)

const {categories} = blogStore

const userInfo = ref({
  username: '',
  avatar: '',
  description: ''
})

const announcements = ref([])
const announcementsLoading = ref(false)

const articlesForCards = computed(() => {
  return (articles.value || []).map(a => ({
    id: a.id,
    title: a.title,
    description: a.description || a.excerpt || '',
    image: a.image || a.cover || '',
    category: a.category || '',
    pinned: typeof a.pinned === 'string' ? a.pinned : String(a.pinned ?? '0'),
    content: a.content || '',
    createdAt: a.createdAt || a.publishDate || '',
    updatedAt: a.updatedAt || a.publishDate || ''
  }))
})

const getUserInfo = async () => {
  try {
    const response = await request.post('/user/getPersonalInfo')
    if (response.code === 0 && response.data) {
      userInfo.value = response.data
    }
  } catch (error) {
  }
}

const fetchAnnouncements = async () => {
  try {
    announcementsLoading.value = true
    const response = await request.get('/announcement/list')
    if (response && response.code === 0 && Array.isArray(response.data)) {
      announcements.value = response.data.filter(item => item.status === 1)
    } else {
      announcements.value = []
    }
  } catch (error) {
    announcements.value = []
  } finally {
    announcementsLoading.value = false
  }
}

const getCacheKey = () => 'home_articles_cache'
const getCachedArticles = () => {
  try {
    const cached = localStorage.getItem(getCacheKey())
    if (cached) {
      const parsed = JSON.parse(cached)
      if (parsed.version === articlesCache.value.version && 
          Date.now() - parsed.timestamp < cacheExpiryTime) {
        return parsed.data
      }
    }
  } catch (error) {
  }
  return null
}

const saveCachedArticles = (data) => {
  try {
    const cacheData = {data, timestamp: Date.now(), version: articlesCache.value.version}
    localStorage.setItem(getCacheKey(), JSON.stringify(cacheData))
    articlesCache.value = cacheData
  } catch (error) {
  }
}

const fetchArticlesSmart = async (forceRefresh = false) => {
  if (isRefreshing.value && !forceRefresh) return
  if (!forceRefresh) {
    const cached = getCachedArticles()
    if (cached) {
      articles.value = cached
      articlesLoading.value = false
      if (!isRefreshing.value) fetchArticlesFromApi(true)
      return
    }
  }
  await fetchArticlesFromApi(forceRefresh)
}

const fetchArticlesFromApi = async (isSilentRefresh = false) => {
  try {
    if (!isSilentRefresh) {
      articlesLoading.value = true
      articlesError.value = ''
    }
    isRefreshing.value = true
    const response = await request.get('/article')
    if (response && Array.isArray(response.articles)) {
      const processedArticles = response.articles.map(article => ({
        id: article.id,
        title: article.title || '',
        description: article.description || '',
        content: article.content || '',
        image: article.image || '',
        cover: article.image || '',
        category: article.category || '未分类',
        pinned: article.pinned ?? '0',
        createdAt: article.createdAt || ''
      }))
      articles.value = processedArticles
      if (!isSilentRefresh) saveCachedArticles(processedArticles)
    }
  } catch (e) {
    if (!isSilentRefresh) articlesError.value = e?.message || '网络异常'
  } finally {
    if (!isSilentRefresh) articlesLoading.value = false
    isRefreshing.value = false
  }
}

const navigation = (selector) => {
  const pageId = document.querySelector(selector)
  if (pageId) {
    window.scrollTo({
      top: pageId.offsetTop,
      behavior: 'smooth'
    })
  }
}

const typeWriter = () => {
  const currentText = typewriterTexts.value[currentTextIndex.value]
  if (isDeleting.value) {
    displayText.value = currentText.substring(0, displayText.value.length - 1)
    if (displayText.value === '') {
      isDeleting.value = false
      currentTextIndex.value = (currentTextIndex.value + 1) % typewriterTexts.value.length
    }
  } else {
    displayText.value = currentText.substring(0, displayText.value.length + 1)
    if (displayText.value === currentText) {
      setTimeout(() => {
        isDeleting.value = true
      }, 2000)
    }
  }
  const typingSpeed = isDeleting.value ? 100 : 150
  setTimeout(typeWriter, typingSpeed)
}

const getGuShi = () => {
  isDeleting.value = true
  displayText.value = ''
}

const loadMoreArticles = () => {
}

const fetchHomeSettings = async () => {
  try {
    const response = await request.get('/home-settings/get')
    if (response.code === 0 && response.data) {
      const settings = response.data
      if (settings.webTitle) {
        webTitle.value = settings.webTitle
      }
      if (settings.backgroundImage) {
        backgroundImage.value = settings.backgroundImage
      }
      if (settings.typewriterTexts) {
        typewriterTexts.value = settings.typewriterTexts.split(',')
      }
    }
  } catch (error) {
    console.error('获取首页设置失败:', error)
    // 后端服务不可用时，使用默认值
    webTitle.value = '我的博客'
    backgroundImage.value = '/img/back1.png'
    typewriterTexts.value = [
      '你看对面的青山多漂亮',
      '春风得意马蹄疾，一日看尽长安花',
      '山重水复疑无路，柳暗花明又一村',
      '会当凌绝顶，一览众山小',
      '海上生明月，天涯共此时',
      '落红不是无情物，化作春泥更护花',
      '长风破浪会有时，直挂云帆济沧海',
      '天生我材必有用，千金散尽还复来'
    ]
  }
}

const onImageError = () => {
  imageLoadAttempts.value++
  if (imageLoadAttempts.value < maxImageLoadAttempts && imageLoadAttempts.value < fallbackImages.length) {
    backgroundImage.value = fallbackImages[imageLoadAttempts.value]
  }
}

const onImageLoad = () => {
  const backgroundElement = document.querySelector('.background-image-index')
  if (backgroundElement) {
    backgroundElement.style.display = 'block'
  }
}

onMounted(async () => {
  await Promise.all([
    fetchArticlesSmart(),
    blogStore.fetchCategories(),
    getUserInfo(),
    fetchAnnouncements(),
    fetchHomeSettings(),
  ])
  setTimeout(() => {
    typeWriter()
  }, 1000)
})
</script>

<style scoped>
#bannerWave1 {
  height: 84px;
  background: url('/img/bannerWave1.png') repeat-x;
  position: absolute;
  width: 200%;
  bottom: 0;
  left: 0;
  z-index: 10;
  animation: gradientBG 120s linear infinite;
}

#bannerWave2 {
  height: 100px;
  background: url('/img/bannerWave2.png') repeat-x;
  position: absolute;
  width: 400%;
  bottom: 0;
  left: 0;
  z-index: 15;
  animation: gradientBG 30s linear infinite;
}

@keyframes gradientBG {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.jiujiu-home-page {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.jiujiu-background {
  position: relative;
  width: 100%;
  height: 50vh;
  overflow: hidden;
  background-color: #fff;
}

.background-image-index {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
  animation: header-effect 2s;
}

.background-image-index::before {
  position: absolute;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.2);
  content: '';
}

@keyframes header-effect {
  0% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.signature-wall {
  display: flex;
  flex-direction: column;
  position: relative;
  user-select: none;
  height: 100%;
  justify-content: center;
  align-items: center;
  z-index: 20;
}

.playful {
  color: #fff;
  font-size: 40px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.printer {
  cursor: pointer;
  color: #fff;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10px;
  padding: 10px 20px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
}

.printer:hover {
  background: rgba(0, 0, 0, 0.5);
  transform: translateY(-2px);
}

.printer-content h3 {
  margin: 0;
  font-size: 18px;
  font-weight: normal;
}

.cursor {
  margin-left: 1px;
  animation: blink 1s infinite;
  font-weight: 200;
  color: #fff;
}

.el-icon-arrow-down {
  font-size: 40px;
  font-weight: bold;
  color: #fff;
  position: absolute;
  bottom: 70px;
  left: 50%;
  transform: translateX(-50%);
  animation: my-shake 1.5s ease-out infinite;
  z-index: 25;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

@keyframes my-shake {
  0%, 100% {
    transform: translate(-50%, 0);
  }
  50% {
    transform: translate(-50%, -10px);
  }
}

.page-container-wrap {
  background: #fff;
  position: relative;
  z-index: 20;
  padding-top: 20px;
}

.page-container {
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 1200px;
  padding: 0 20px 40px;
  margin: 0 auto;
  flex-direction: row;
  gap: 20px;
}

.recent-posts {
  flex: 1;
  min-width: 0;
}

.aside-content {
  width: 280px;
  flex-shrink: 0;
}

.jiujiu-aside-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  margin-bottom: 16px;
  color: #fff;
}

.profile-card {
  text-align: center;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 5px;
}

.profile-signature {
  font-size: 13px;
  opacity: 0.9;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-number {
  font-size: 18px;
  font-weight: bold;
  display: block;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
}

.social-links {
  display: flex;
  justify-content: space-around;
  padding-top: 15px;
  margin-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.social-link {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.social-link:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.4);
}

.announcement {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 12px;
  display: flex;
  margin-bottom: 20px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  align-items: center;
  gap: 10px;
  color: #666;
}

.announcement-icon {
  color: #E6162D;
  animation: scale 1s ease-in-out infinite alternate;
}

@keyframes scale {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.1);
  }
}

.jiujiu-articles-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.articles-loading {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-skeleton {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #eee;
}

.skeleton-image {
  width: 120px;
  height: 80px;
  background: #f0f0f0;
  border-radius: 8px;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-title {
  height: 20px;
  background: #f0f0f0;
  border-radius: 4px;
}

.skeleton-description {
  height: 16px;
  background: #f0f0f0;
  border-radius: 4px;
}

.pagination-wrap {
  text-align: center;
  margin-top: 30px;
}

.pagination {
  padding: 8px 25px;
  border: 1px solid #eee;
  border-radius: 20px;
  color: #666;
  cursor: pointer;
  display: inline-block;
  transition: all 0.3s;
  background: #fff;
}

.pagination:hover {
  border-color: #764ba2;
  color: #764ba2;
  box-shadow: 0 4px 12px rgba(118, 75, 162, 0.2);
}

@media (max-width: 1200px) {
  .jiujiu-articles-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .jiujiu-background {
    height: 40vh;
  }

  .page-container {
    flex-direction: column;
  }

  .aside-content {
    width: 100%;
    order: 2;
  }

  .recent-posts {
    order: 1;
  }

  .jiujiu-articles-list {
    grid-template-columns: 1fr;
  }

  #bannerWave1 {
    height: 60px;
  }

  #bannerWave2 {
    height: 70px;
  }

  .playful {
    font-size: 28px;
  }

  .printer-content h3 {
    font-size: 14px;
  }
}

.profile-card {
  text-align: center;
  padding: 10px 0;
  position: relative;
  z-index: 1;
}

/* 头像区域 */
.profile-avatar-wrap {
  margin-bottom: 15px;
  position: relative;
  display: inline-block;
}

.user-avatar {
  border: 4px solid rgba(255, 255, 255, 0.3); /* 半透明白边框 */
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.2); /* 发光效果 */
  transition: all 0.5s ease-in-out;
  cursor: pointer;
  background: #fff;
}

/* 头像悬停旋转动画 */
.user-avatar:hover {
  transform: rotate(360deg) scale(1.05);
  border-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
}

.profile-info {
  margin-bottom: 20px;
}

.profile-name {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fee140 0%, #fa709a 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: none;
  letter-spacing: 1.5px;
  background-size: 200% auto;
  animation: gradientText 3s linear infinite;
}

.profile-signature {
  font-size: 14px;
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;

  margin: 0;
  font-family: 'Fredoka', sans-serif;
  padding: 0 10px;
  line-height: 1.5;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

@keyframes gradientText {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin-top: 15px;
  padding: 15px 0;
  position: relative;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.stat-item {
  flex: 1;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.stat-item:first-child::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 60%;
  width: 1px;
  background: rgba(255, 255, 255, 0.2);
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-item:hover .stat-number {
  color: #fff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
}

.stat-number {
  display: block;
  font-size: 20px;
  font-weight: 800;
  color: #ffffff;
  margin-bottom: 4px;
  font-family: 'Fredoka', sans-serif;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
}
</style>