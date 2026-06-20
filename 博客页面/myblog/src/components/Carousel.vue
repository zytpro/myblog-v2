<template>
  <div class="carousel-container">
    <v-carousel
      v-model="currentSlide"
      :show-arrows="false"
      :show-dots="true"
      :interval="5000"
      height="400"
      cycle
      hide-delimiter-background
      delimiter-icon="mdi-circle"
      class="carousel"
    >
      <v-carousel-item
        v-for="(item, index) in carouselItems"
        :key="index"
        class="carousel-item"
        @click="handleItemClick(item)"
      >
        <div class="carousel-content">
          <div class="image-overlay-bottom"></div>
          <v-img
            :src="item.image"
            :alt="item.title"
            class="carousel-image"
            cover
          >
            <template v-slot:placeholder>
              <div class="image-placeholder">
                <v-icon size="64" color="white">mdi-image</v-icon>
              </div>
            </template>
          </v-img>
          
          <div class="carousel-text">
            <h2 class="carousel-title">{{ item.title }}</h2>
            <p class="carousel-description" v-if="item.description">{{ item.description }}</p>
            <v-btn
              v-if="item.link && item.link !== '#'"
              class="read-more-btn gradient-pill"
              @click.stop="handleItemClick(item)"
              rounded="pill"
              size="large"
              variant="flat"
            >
              阅读更多
              <v-icon end>mdi-arrow-right</v-icon>
            </v-btn>
          </div>
        </div>
      </v-carousel-item>
    </v-carousel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utills/request.js'

const router = useRouter()
const currentSlide = ref(0)

// 轮播图数据（从后端获取）
const carouselItems = ref([])

const normalizeImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `${request.defaults.baseURL}${url}`
}

const fetchCarousel = async () => {
  try {
    const res = await request.get('/carousel')
    let list = Array.isArray(res) ? res : (res?.data || [])
    // 仅显示启用项（status === 1）
    list = list.filter(i => i.status === 1 || i.status === '1')
    carouselItems.value = list.map(i => ({
      id: i.id,
      title: i.title || '精彩推荐',
      description: i.description || '',
      image: normalizeImageUrl(i.imageURL || i.image || ''),
      link: i.articleId ? `/article/${i.articleId}` : '#'
    }))
  } catch (e) {
    carouselItems.value = []
  }
}

const handleItemClick = (item) => {
  if (item?.link && item.link !== '#') router.push(item.link)
}

onMounted(fetchCarousel)
</script>

<style scoped>
.carousel-container {
  margin: 0;
  width: 100%;
  max-width: 100%;
  padding: 0;
}

.carousel {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 100%;
  margin: 0;
  position: relative;
  z-index: 1;
}

.carousel-item {
  position: relative;
  cursor: pointer;
}

.carousel-content {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* 底部渐变遮罩，贴近示例效果 */
.image-overlay-bottom {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 55%;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.58) 0%,
    rgba(0, 0, 0, 0.45) 35%,
    rgba(0, 0, 0, 0.18) 65%,
    rgba(0, 0, 0, 0) 100%
  );
  z-index: 2;
}

/* 标题与按钮布局在左下角 */
.carousel-text {
  position: absolute;
  left: 40px;
  bottom: 28px;
  z-index: 3;
  text-align: left;
  color: #fff;
  max-width: 680px;
}

.carousel-title {
  font-size: 2rem;
  font-weight: 800;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.45);
}

.carousel-description {
  font-size: 1rem;
  margin: 0 0 16px 0;
  opacity: 0.95;
}

/* 胶囊形粉紫渐变按钮 */
.gradient-pill {
  background: linear-gradient(90deg, #ff8ad5 0%, #b36bff 100%) !important;
  color: #fff !important;
  border: none !important;
  box-shadow: 0 6px 18px rgba(179, 107, 255, 0.35);
}

.read-more-btn {
  font-weight: 700;
  text-transform: none;
  padding: 12px 22px;
}

.read-more-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 22px rgba(179, 107, 255, 0.45);
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 调整分页圆点样式，靠近底部中间区域的长条与圆点组合感 */
:deep(.v-carousel__controls) {
  padding-bottom: 8px;
}

:deep(.v-carousel__controls .v-btn--icon) {
  width: 44px; /* 扩大点击区域，便于切换 */
  height: 12px;
  margin: 0 6px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 999px;
  cursor: pointer;
}

:deep(.v-carousel__controls .v-btn--icon.v-btn--active) {
  width: 64px;
  background: linear-gradient(90deg, #ff8ad5 0%, #b36bff 100%);
}

/* 隐藏默认白色圆点图标，仅显示我们自定义的条形背景 */
:deep(.v-carousel__controls .v-btn--icon .v-btn__content),
:deep(.v-carousel__controls .v-btn--icon .v-icon) {
  display: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .carousel-text { left: 16px; right: 16px; bottom: 16px; max-width: none; }
  .carousel-title { font-size: 1.6rem; }
  .carousel-description { font-size: 0.95rem; margin-bottom: 10px; }
}

@media (max-width: 480px) {
  .carousel-title { font-size: 1.35rem; }
  .carousel-description { font-size: 0.9rem; }
}
</style> 