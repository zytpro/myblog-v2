<template>
  <div class="article-page">
    <div class="article-header">
      <div
          class="header-cover"
          :style="{ backgroundImage: `url(${article.cover || 'https://img.fernet.io/img/202402/20240212155720.jpg'})` }"
      ></div>
      <div class="header-mask"></div>

      <div class="header-info my-animation-slide-top">
        <h1 class="article-title">{{ article.title || '正在加载文章...' }}</h1>
        <div class="article-meta">
          <span class="meta-item"><i class="el-icon-date"></i> {{ article.createTime || '...' }}</span>
          <span class="meta-separator">|</span>
          <span class="meta-item"><i class="el-icon-view"></i> {{ article.views || 0 }} 阅读</span>
          <span class="meta-separator">|</span>
          <span class="meta-item"><i class="el-icon-chat-dot-round"></i> {{ article.comments || 0 }} 评论</span>
          <span class="meta-separator">|</span>
          <span class="meta-item"><i class="el-icon-star-off"></i> {{ article.likes || 0 }} 点赞</span>
        </div>
      </div>

      <div class="header-wave">
        <svg viewBox="0 0 1440 320" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="none">
          <path fill="#f5f7fa" fill-opacity="1"
                d="M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,112C672,96,768,96,864,112C960,128,1056,160,1152,160C1248,160,1344,128,1392,112L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
      </div>
    </div>

    <div class="main-container">
      <div class="content-wrapper my-animation-slide-bottom">

        <div class="left-column">
          <div class="card article-card">
            <ArticleDetail :article="article" contenteditable="false"/>

            <div class="article-footer-info">
              <div class="article-tags" v-if="article.tags && article.tags.length">
                <el-tag
                    v-for="(tag, index) in article.tags"
                    :key="index"
                    size="small"
                    effect="plain"
                    class="tag-item"
                ># {{ tag }}
                </el-tag>
              </div>
              <div class="article-copyright">
                <p><strong>文章作者：</strong>{{ userInfo.username || 'Poetize' }}</p>
                <p><strong>版权声明：</strong>本博客所有文章除特别声明外，均采用 BY-NC-SA 许可协议。转载请注明出处！</p>
              </div>
            </div>
          </div>

          <div class="card comments-section">
            <h3 class="comments-title">
              <span class="icon">💬</span> 评论 ({{ comments.length }})
            </h3>

            <div class="comment-input-area">
              <div class="user-avatar">
                <img :src="userInfo.avatar || defaultAvatar" alt="User Avatar"/>
              </div>
              <div class="input-wrapper">
                <el-input
                    v-model="newComment"
                    type="textarea"
                    :rows="3"
                    placeholder="发一条友善的评论..."
                    resize="none"
                    class="custom-textarea"
                />
                <div class="comment-actions-bar">
                  <span class="emoji-btn">😊 表情</span>
                  <el-button type="primary" size="small" round @click="submitComment">发送评论</el-button>
                </div>
              </div>
            </div>

            <div class="comments-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-avatar">
                  <img :src="comment.avatar || defaultAvatar" :alt="comment.username"/>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="username">{{ comment.username }}</span>
                    <span class="comment-time">{{ comment.time }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                  <div class="comment-footer">
                    <span class="reply-btn">回复</span>
                  </div>
                </div>
              </div>

              <div v-if="comments.length === 0" class="no-comments">
                <p>还没有人评论，快来抢沙发~</p>
              </div>
            </div>
          </div>
        </div>

        <div class="right-column">
          <div class="card sidebar-card author-card">
            <div class="card-bg-img"></div>

            <div class="author-avatar">
              <img :src="userInfo.avatar || defaultAvatar" :alt="userInfo.username">
            </div>
            <div class="author-name">{{ userInfo.username || 'Blog Owner' }}</div>
            <div class="author-desc">{{ userInfo.description || '万物皆有裂痕，那是光照进来的地方' }}</div>

            <div class="author-stats">
              <div class="stat-box">
                <span>文章</span>
                <strong>{{ totalArticles }}</strong>
              </div>
              <div class="stat-box">
                <span>分类</span>
                <strong>{{ totalCategories }}</strong>
              </div>
            </div>

            <div class="author-social">
              <i class="el-icon-chat-dot-round" title="QQ"></i>
              <i class="el-icon-share" title="Weibo"></i>
              <i class="el-icon-link" title="Github"></i>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import ArticleDetail from '../components/ArticleDetail.vue';
import request from '../utills/request.js';
import { ElMessage } from 'element-plus';

export default {
  components: {
    ArticleDetail
  },
  data() {
    return {
      article: {
        title: '',
        description: '',
        content: '',
        likes: 0,
        stars: 0,
        comments: 0,
        views: 0,
        cover: '',
        createTime: '',
        tags: []
      },
      newComment: '',
      comments: [],
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',

      userInfo: {
        username: '加载中...',
        avatar: '',
        description: ''
      },
      totalArticles: 0,
      totalCategories: 0
    };
  },
  mounted() {
    this.fetchArticle();
    this.fetchComments();
    this.fetchUserInfo();
    this.fetchSiteStats();
    this.ensurePrism().then(() => this.highlightCodes());
  },
  methods: {
    async ensurePrism() {
      if (window.Prism) return;
      const add = (tag, attrs) => {
        return new Promise((resolve, reject) => {
          const el = document.createElement(tag)
          Object.entries(attrs || {}).forEach(([k, v]) => el.setAttribute(k, v))
          el.onload = () => resolve()
          el.onerror = () => resolve()
          document.head.appendChild(el)
        })
      }
      await add('link', { rel: 'stylesheet', href: 'https://cdn.jsdelivr.net/npm/prismjs@1.29.0/themes/prism-tomorrow.min.css' })
      await add('script', { src: 'https://cdn.jsdelivr.net/npm/prismjs@1.29.0/prism.min.js' })
      await add('script', {src: 'https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-java.min.js'})
      await add('script', { src: 'https://cdn.jsdelivr.net/npm/prismjs@1.29.0/components/prism-javascript.min.js' })
    },
    highlightCodes() {
      if (window.Prism) {
        window.Prism.highlightAllUnder(this.$el)
      }
    },

    async fetchArticle() {
      const articleId = this.$route.params.id;
      if (!articleId) return;
      try {
        const response = await request.get(`/article/${articleId}`);
        if (response.id !== null) {
          this.article = {
            ...response,
            likes: response.likes || 0,
            stars: response.stars || 0,
            comments: response.comments || 0,
            views: response.views || 0,
            cover: response.image || 'https://img.fernet.io/img/202402/20240212155720.jpg',
            tags: response.tags ? response.tags.split(',') : []
          };
          this.$nextTick(() => this.highlightCodes())
        }
      } catch (error) {
        console.error("获取文章数据失败:", error);
      }
    },

    async fetchComments() {
      const articleId = this.$route.params.id;
      try {
        const response = await request.get(`/comments/all/${articleId}`);
        if (Array.isArray(response)) {
          this.comments = response.map(comment => ({
            id: comment.id,
            content: comment.content,
            username: comment.nickname || '访客',
            time: this.formatCommentTime(comment.createdAt),
            avatar: comment.avatar || this.defaultAvatar
          }));
        }
      } catch (error) {
        console.error("获取评论失败:", error);
      }
    },

    async submitComment() {
      if (!this.newComment.trim()) {
        ElMessage.warning('评论内容不能为空');
        return;
      }
      try {
        const response = await request.post('/comments', {
          articleId: Number(this.$route.params.id),
          content: this.newComment.trim(),
          status: 1
        });

        if (response.code === 0 || response === "评论成功") {
          this.newComment = '';
          await this.fetchComments();
          this.article.comments = (this.article.comments || 0) + 1;
          ElMessage.success('评论成功');
        } else {
          ElMessage.error(typeof response === 'string' ? response : (response.message || '评论失败'));
        }
      } catch (error) {
        ElMessage.error('评论失败');
      }
    },

    async fetchUserInfo() {
      try {
        const response = await request.post('/user/getPersonalInfo');
        if (response.code === 0 && response.data) {
          this.userInfo = response.data;
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
      }
    },

    async fetchSiteStats() {
      try {
        const [articleRes, categoryRes] = await Promise.all([
          request.get('/article'),
          request.get('/categories')
        ]);

        if (articleRes && Array.isArray(articleRes.articles)) {
          this.totalArticles = articleRes.articles.length;
        } else if (Array.isArray(articleRes)) {
          this.totalArticles = articleRes.length;
        }

        if (Array.isArray(categoryRes)) {
          this.totalCategories = categoryRes.length;
        } else if (categoryRes && Array.isArray(categoryRes.data)) {
          this.totalCategories = categoryRes.data.length;
        }
      } catch (error) {
        console.error("获取站点统计失败:", error);
      }
    },

    formatCommentTime(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
      });
    }
  }
};
</script>

<style scoped>
.article-page {
  background-color: #f5f7fa;
  min-height: 100vh;
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.article-header {
  position: relative;
  width: 100%;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  color: #fff;
  text-align: center;
}

.header-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  z-index: 0;
  animation: scale 10s infinite alternate;
}

@keyframes scale {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.05);
  }
}

.header-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.header-info {
  position: relative;
  z-index: 2;
  padding: 0 20px;
  max-width: 800px;
}

.article-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  letter-spacing: 1px;
}

.article-meta {
  font-size: 0.95rem;
  opacity: 0.9;
}

.meta-item i {
  margin-right: 4px;
}

.meta-separator {
  margin: 0 10px;
  opacity: 0.6;
}

.header-wave {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  z-index: 3;
  line-height: 0;
}

.header-wave svg {
  display: block;
  width: 100%;
  height: 60px;
}

.main-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px 20px;
  position: relative;
  z-index: 4;
}

.content-wrapper {
  display: flex;
  gap: 20px;
  margin-top: -60px; 
}

.left-column {
  flex: 1;
  min-width: 0; 
}

.right-column {
  width: 300px;
  flex-shrink: 0;
}

.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 16px -4px rgba(44, 45, 48, 0.047);
  overflow: hidden;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.card:hover {
  box-shadow: 0 12px 24px -4px rgba(44, 45, 48, 0.1);
}

.article-card {
  padding: 40px;
  min-height: 500px;
}

.article-footer-info {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px dashed #eee;
}

.tag-item {
  margin-right: 10px;
}

.article-copyright {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-left: 4px solid #667eea;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #666;
}

.article-copyright p {
  margin: 5px 0;
}

.comments-section {
  padding: 30px;
}

.comments-title {
  font-size: 1.3rem;
  font-weight: 700;
  margin-bottom: 25px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-input-area {
  display: flex;
  gap: 15px;
  margin-bottom: 40px;
}

.user-avatar {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.input-wrapper {
  flex: 1;
  position: relative;
}

.custom-textarea :deep(.el-textarea__inner) {
  background-color: #f6f8fa;
  border: 1px solid #f6f8fa;
  border-radius: 8px;
  transition: all 0.3s;
  padding: 15px;
}

.custom-textarea :deep(.el-textarea__inner):focus {
  background-color: #fff;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 238, 0.1);
}

.comment-actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.emoji-btn {
  font-size: 0.9rem;
  color: #666;
  cursor: pointer;
}

.emoji-btn:hover {
  color: #667eea;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar img {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  object-fit: cover;
}

.comment-item:hover .comment-avatar img {
  transform: rotate(360deg);
}

.comment-body {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
}

.comment-header .username {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
  margin-right: 10px;
}

.comment-header .comment-time {
  font-size: 0.8rem;
  color: #999;
}

.comment-text {
  color: #555;
  line-height: 1.6;
  font-size: 0.95rem;
  background: #f7f7f7;
  padding: 10px 15px;
  border-radius: 8px;
  position: relative;
  display: inline-block;
  max-width: 100%;
}

.comment-text::before {
  content: '';
  position: absolute;
  top: 10px;
  left: -6px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 6px solid #f7f7f7;
}

.comment-footer {
  margin-top: 8px;
}

.reply-btn {
  font-size: 0.85rem;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}

.reply-btn:hover {
  color: #667eea;
  text-decoration: underline;
}

.no-comments {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.sidebar-card {
  padding: 0;
  overflow: visible;
  background: #fff;
  position: relative;
}

.card-bg-img {
  height: 120px;
  width: 100%;
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  border-radius: 12px 12px 0 0;
}

.author-card {
  text-align: center;
  padding-bottom: 25px;
}

.author-avatar {
  width: 80px;
  height: 80px;
  margin: -40px auto 10px;
  position: relative;
  z-index: 2;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.5s;
  object-fit: cover;
  background-color: #fff;
}

.author-card:hover .author-avatar img {
  transform: rotate(360deg);
}

.author-name {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.author-desc {
  font-size: 0.9rem;
  color: #666;
  margin: 0 20px 20px;
  line-height: 1.5;
  min-height: 40px;
}

.author-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 20px;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-box:hover {
  transform: translateY(-3px);
  color: #667eea;
}

.stat-box span {
  font-size: 0.85rem;
  color: #888;
  margin-bottom: 4px;
}

.stat-box strong {
  font-size: 1.2rem;
  color: #333;
  font-family: Arial, sans-serif;
}

.stat-box:hover strong {
  color: #667eea;
}

.author-social {
  display: flex;
  justify-content: center;
  gap: 25px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
  margin: 0 20px;
}

.author-social i {
  font-size: 1.3rem;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.author-social i:hover {
  color: #667eea;
  transform: scale(1.2);
}


.sidebar-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.toc-placeholder {
  color: #999;
  font-size: 0.9rem;
  padding: 10px 0;
  text-align: center;
}

@media (max-width: 1000px) {
  .content-wrapper {
    flex-direction: column;
  }

  .right-column {
    width: 100%;
  }

  .article-title {
    font-size: 1.8rem;
  }
}

.my-animation-slide-top {
  animation: slideTop 1s ease both;
}

.my-animation-slide-bottom {
  animation: slideBottom 1s ease both 0.3s;
}

@keyframes slideTop {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideBottom {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

</style>