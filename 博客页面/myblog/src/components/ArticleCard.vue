<template>
  <v-card
      class="article-card"
      elevation="0"
      :ripple="false"
      @click="viewArticle"
  >
    <!-- 图片区域 -->
    <div class="article-cover-wrap">
      <img :src="article.image" class="article-cover" loading="lazy"/>
      <div class="article-overlay"></div>
    </div>

    <!-- 内容区域 -->
    <div class="article-info">
      <div class="article-meta">
        <span class="category-tag" v-if="article.category">
          <i class="el-icon-folder"></i> {{ article.category }}
        </span>
        <span class="time-tag" v-if="article.createdAt">
          <i class="el-icon-date"></i> {{ formatDate(article.createdAt) }}
        </span>
        <span class="pinned-tag" v-if="article.pinned === '1'">
          <i class="el-icon-top"></i> 置顶
        </span>
      </div>

      <h3 class="article-title">{{ article.title }}</h3>

      <p class="article-desc">{{ article.description }}</p>

      <div class="article-footer">
        <span class="read-more">阅读全文 →</span>
      </div>
    </div>
  </v-card>
</template>

<script setup>
import {useRouter} from 'vue-router';

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
});

const router = useRouter();

const viewArticle = () => {
  router.push(`/article/${props.article.id}`);
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};
</script>

<style scoped>
.article-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #eee;
  height: 100%;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: transparent;
}

.article-cover-wrap {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.article-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.article-card:hover .article-cover {
  transform: scale(1.05);
}

.article-info {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.article-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  align-items: center;
}

.category-tag {
  color: #764ba2;
  background: rgba(118, 75, 162, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.pinned-tag {
  color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.article-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
  line-height: 1.4;
  transition: color 0.3s;
}

.article-card:hover .article-title {
  color: #764ba2;
}

.article-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.article-footer {
  margin-top: auto;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.read-more {
  font-size: 13px;
  font-weight: 600;
  color: #764ba2;
}
</style>