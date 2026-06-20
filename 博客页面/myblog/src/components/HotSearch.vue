<template>
  <div class="page-wrapper">
    <!-- 背景气泡动画 -->
    <ul class="bg-bubbles">
      <li v-for="n in 10" :key="n"></li>
    </ul>

    <div class="hot-search-container">
      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="logo-area">
          <h1 class="main-title">Discovery</h1>
          <p class="sub-title">探索全网热点</p>
        </div>

        <div class="search-box-wrapper">
          <div class="input-container">
            <span class="search-icon">🔍</span>
            <input 
              v-model="searchText"
              @keyup.enter="handleSearch"
              placeholder="搜索你感兴趣的内容..."
              type="text"
              class="custom-input"
            >
            <button class="search-btn" @click="handleSearch">
              搜索
            </button>
          </div>

          <!-- 搜索引擎切换 -->
          <div class="search-engines">
            <div 
              v-for="engine in searchEngines" 
              :key="engine.name"
              class="engine-chip"
              :class="{ active: currentEngine === engine.name }"
              @click="selectEngine(engine.name)"
            >
              <img :src="engine.icon" :alt="engine.name">
              <span>{{ engine.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 热搜榜单卡片 -->
      <div class="glass-card">
        <div class="card-header">
          <div class="header-title">
            <span class="fire-icon">🔥</span>
            <h2>实时热榜</h2>
          </div>

          <!-- 自定义 Tab 切换 -->
          <div class="custom-tabs">
            <div
                v-for="item in apiList"
                :key="item.name"
                class="tab-item"
                :class="{ active: currentTab === getTabKey(item.name) }"
                @click="switchTab(getTabKey(item.name))"
            >
              {{ item.name.replace('热搜', '').replace('热点新闻', '').replace('热榜', '') }}
            </div>
          </div>

          <button class="refresh-btn" @click="refreshData" :disabled="loading">
            <span :class="{ rotating: loading }">↻</span> 刷新
          </button>
        </div>

        <div class="card-body">
          <!-- 列表内容 -->
          <transition-group name="list" tag="ul" class="hot-list" v-if="currentList.length">
            <li
                v-for="(item, index) in currentList"
                :key="item.title"
                class="hot-item"
                @click="openUrl(item.url)"
                :style="{ '--i': index }"
            >
              <div class="item-rank" :class="getRankClass(index)">
                {{ index + 1 }}
              </div>

              <div class="item-content">
                <div class="item-main">
                  <span class="item-title">{{ item.title }}</span>
                  <span v-if="index < 3" class="hot-badge">HOT</span>
                </div>
                <div class="item-desc" v-if="item.desc">{{ item.desc }}</div>
              </div>

              <div class="item-meta">
                <span class="hot-value">{{ formatHot(item.hot) }}</span>
                <img v-if="currentTab === 'baidu' && item.pic" :src="item.pic" class="item-thumb">
              </div>
            </li>
          </transition-group>

          <!-- 加载中 -->
          <div v-else-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>正在获取最新热点...</p>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-state">
            <div class="empty-emoji">🍂</div>
            <p>暂无数据，请稍后再试</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import request from '../utills/request.js';

export default {
  name: "HotSearch",
  setup() {
    const weiboList = ref([]);
    const baiduList = ref([]);
    const loading = ref(false);
    const currentTab = ref('weibo');
    const apiConfig = ref({});
    const apiList = ref([]);

    const currentList = computed(() => {
      return currentTab.value === 'weibo' ? weiboList.value : baiduList.value;
    });

    const getTabKey = (name) => {
      const keyMap = {
        '微博热搜': 'weibo',
        '百度今日热点新闻': 'baidu',
        '知乎热搜': 'zhihu',
        '抖音热搜': 'douyin',
        '抖音热榜': 'douyin'
      };
      return keyMap[name] || name.toLowerCase();
    };

    // 获取排名样式类
    const getRankClass = (index) => {
      if (index === 0) return 'rank-1';
      if (index === 1) return 'rank-2';
      if (index === 2) return 'rank-3';
      return '';
    };

    const fetchApiConfig = async () => {
      try {
        const response = await request.get('/api');
        if (response.code === 0 && Array.isArray(response.data)) {
          apiList.value = response.data;
          const config = {};
          response.data.forEach(item => {
            const key = getTabKey(item.name);
            config[key] = item.api;
          });
          apiConfig.value = config;

          if (!apiConfig.value[currentTab.value]) {
            const firstAvailableTab = Object.keys(apiConfig.value)[0];
            if (firstAvailableTab) currentTab.value = firstAvailableTab;
          }
        }
      } catch (error) {
        console.error('Config Error:', error);
      }
    };

    const formatters = {
      weibo: (data) => data.code === 1 ? data.data.map(item => ({
        title: item.title,
        hot: item.hot,
        url: item.url || item.mobilUrl,
        desc: item.desc,
        pic: item.pic
      })) : [],
      baidu: (data) => data.code === 1 ? data.data.map(item => ({
        title: item.title,
        hot: item.hot,
        url: item.url || item.mobilUrl,
        desc: item.desc,
        pic: item.pic
      })) : [],
      douyin: (data) => (data.word_list || []).map(item => ({
        title: item.word,
        hot: item.hot_value,
        url: `https://www.douyin.com/search/${encodeURIComponent(item.word)}`,
      })),
      zhihu: (data) => data.data.map(item => ({
        title: item.target.title,
        hot: item.detail_text,
        url: `https://www.zhihu.com/question/${item.target.id}`,
        desc: item.target.excerpt || '',
        pic: item.children?.[0]?.thumbnail || ''
      }))
    };

    const CACHE_EXPIRY = 60 * 60 * 1000;
    const cacheSystem = {
      get: (key) => {
        const cached = localStorage.getItem(key);
        if (!cached) return null;
        const { data, timestamp } = JSON.parse(cached);
        if (Date.now() - timestamp > CACHE_EXPIRY) {
          localStorage.removeItem(key);
          return null;
        }
        return data;
      },
      set: (key, data) => {
        localStorage.setItem(key, JSON.stringify({data, timestamp: Date.now()}));
      }
    };

    const fetchData = async (forceRefresh = false) => {
      if (!apiConfig.value[currentTab.value]) await fetchApiConfig();
      
      const cacheKey = `hotSearch_${currentTab.value}`;
      if (!forceRefresh) {
        const cachedData = cacheSystem.get(cacheKey);
        if (cachedData) {
          currentTab.value === 'weibo' ? weiboList.value = cachedData : baiduList.value = cachedData;
          return;
        }
      }

      loading.value = true;
      try {
        const apiUrl = apiConfig.value[currentTab.value];
        if (!apiUrl) return;

        const response = await request(`/api/getHotSearch?url=${apiUrl}`);
        if (response.code === 0) {
          const parsedData = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;
          const formatter = formatters[currentTab.value];
          if (formatter) {
            const formattedData = formatter(parsedData);
            currentTab.value === 'weibo' ? weiboList.value = formattedData : baiduList.value = formattedData;
            cacheSystem.set(cacheKey, formattedData);
          }
        }
      } catch (error) {
        console.error('Fetch Error:', error);
      } finally {
        loading.value = false;
      }
    };

    const switchTab = async (tab) => {
      if (currentTab.value !== tab) {
        currentTab.value = tab;
        await fetchData(false);
      }
    };

    const refreshData = () => fetchData(true);

    const formatHot = (hot) => {
      if (!hot) return '';
      if (String(hot).includes('万')) return hot;
      const num = Number(hot);
      return !isNaN(num) && num >= 10000 ? (num / 10000).toFixed(1) + '万' : hot;
    };

    const openUrl = (url) => url && window.open(url, '_blank');

    // 搜索逻辑
    const searchText = ref('');
    const currentEngine = ref('百度');
    const searchEngines = [
      {name: '百度', url: 'https://www.baidu.com/s?wd=', icon: 'https://www.baidu.com/favicon.ico'},
      {name: 'Google', url: 'https://www.google.com/search?q=', icon: 'https://www.google.com/favicon.ico'},
      {name: '微博', url: 'https://s.weibo.com/weibo?q=', icon: 'https://weibo.com/favicon.ico'},
      {name: '知乎', url: 'https://www.zhihu.com/search?q=', icon: 'https://static.zhihu.com/heifetz/favicon.ico'},
      {
        name: '抖音',
        url: 'https://www.douyin.com/search/',
        icon: 'https://lf1-cdn-tos.bytegoofy.com/goofy/ies/douyin_web/public/favicon.ico'
      },
      {name: 'CSDN', url: 'https://so.csdn.net/so/search?q=', icon: 'https://g.csdnimg.cn/static/logo/favicon32.ico'},
      {name: 'GitHub', url: 'https://github.com/search?q=', icon: 'https://github.com/favicon.ico'}
    ];

    const handleSearch = () => {
      if (!searchText.value.trim()) return;
      const engine = searchEngines.find(e => e.name === currentEngine.value);
      if (engine) {
        const query = engine.name === '抖音' ? encodeURIComponent(searchText.value.trim()) : encodeURIComponent(searchText.value.trim());
        window.open(engine.url + query, '_blank');
      }
    };

    const selectEngine = (name) => currentEngine.value = name;

    onMounted(async () => {
      await fetchApiConfig();
      await fetchData();
    });

    return {
      currentList, loading, currentTab, apiList, searchText, currentEngine, searchEngines,
      getTabKey, switchTab, refreshData, formatHot, openUrl, handleSearch, selectEngine, getRankClass
    };
  }
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fredoka:wght@400;600&family=Noto+Sans+SC:wght@400;500;700&display=swap');

.page-wrapper {
  min-height: 100vh;
  width: 100%;
  font-family: 'Fredoka', 'Noto Sans SC', sans-serif;
  /* Poetize 经典配色 */
  background-image: linear-gradient(120deg, #fccb90 0%, #d57eeb 100%);
  position: relative;
  overflow-x: hidden;
  color: #555;
}

/* 气泡背景动画 */
.bg-bubbles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  margin: 0;
  padding: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-bubbles li {
  position: absolute;
  list-style: none;
  display: block;
  width: 40px;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.15);
  bottom: -160px;
  animation: square 25s infinite;
  transition-timing-function: linear;
  border-radius: 50%;
}

.bg-bubbles li:nth-child(1) {
  left: 10%;
  width: 80px;
  height: 80px;
  animation-delay: 0s;
}

.bg-bubbles li:nth-child(2) {
  left: 20%;
  width: 40px;
  height: 40px;
  animation-delay: 2s;
  animation-duration: 17s;
}

.bg-bubbles li:nth-child(3) {
  left: 25%;
  animation-delay: 4s;
}

.bg-bubbles li:nth-child(4) {
  left: 40%;
  width: 60px;
  height: 60px;
  animation-duration: 22s;
  background-color: rgba(255, 255, 255, 0.25);
}

.bg-bubbles li:nth-child(5) {
  left: 70%;
}

@keyframes square {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-120vh) rotate(600deg);
  }
}

.hot-search-container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 搜索区域 */
.search-section {
  text-align: center;
  margin-bottom: 50px;
}

.logo-area {
  margin-bottom: 30px;
  color: #fff;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.main-title {
  font-family: 'Fredoka', cursive;
  font-size: 3.5rem;
  margin: 0;
  letter-spacing: 2px;
}

.sub-title {
  font-size: 1.1rem;
  opacity: 0.9;
  letter-spacing: 3px;
  margin-top: 5px;
}

.search-box-wrapper {
  max-width: 700px;
  margin: 0 auto;
}

.input-container {
  display: flex;
  background: rgba(255, 255, 255, 0.9);
  padding: 5px;
  border-radius: 50px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.input-container:focus-within {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.8);
}

.search-icon {
  padding: 15px 0 15px 20px;
  font-size: 1.2rem;
  color: #888;
}

.custom-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 15px;
  font-size: 1.1rem;
  color: #444;
  outline: none;
}

.search-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 40px;
  padding: 0 35px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
  margin: 3px;
}

.search-btn:hover {
  transform: scale(1.05);
}

.search-engines {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}

.engine-chip {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(5px);
  padding: 6px 15px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
  font-weight: 500;
  transition: all 0.2s;
  border: 1px solid rgba(255, 255, 255, 0.2);
  user-select: none;
}

.engine-chip img {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}

.engine-chip:hover, .engine-chip.active {
  background: #fff;
  color: #f5576c;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

/* 玻璃卡片通用样式 */
.glass-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 25px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 15px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.fire-icon {
  font-size: 1.8rem;
  animation: pulse 2s infinite;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
  font-weight: 700;
}

/* 自定义 Tabs */
.custom-tabs {
  display: flex;
  background: #f5f5f5;
  padding: 4px;
  border-radius: 30px;
  gap: 5px;
  overflow-x: auto;
}

.tab-item {
  padding: 8px 18px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  color: #777;
  transition: all 0.3s;
  white-space: nowrap;
}

.tab-item.active {
  background: #fff;
  color: #f5576c;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.refresh-btn {
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
  transition: color 0.2s;
}

.refresh-btn:hover {
  color: #f5576c;
}

.rotating {
  display: inline-block;
  animation: rotate 1s linear infinite;
}

/* 列表样式 */
.hot-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #f9f9f9;
}

.hot-item:hover {
  background: #fff5f7;
  transform: translateX(5px);
}

.item-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1rem;
  color: #999;
  margin-right: 15px;
  flex-shrink: 0;
}

/* 前三名样式 */
.item-rank.rank-1 {
  color: #ff4757;
  font-size: 1.4rem;
}

.item-rank.rank-2 {
  color: #ffa502;
  font-size: 1.3rem;
}

.item-rank.rank-3 {
  color: #eccc68;
  font-size: 1.2rem;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-title {
  font-size: 1.05rem;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-badge {
  background: #ff4757;
  color: white;
  font-size: 0.6rem;
  padding: 1px 4px;
  border-radius: 4px;
  font-weight: bold;
}

.item-desc {
  font-size: 0.85rem;
  color: #888;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 15px;
}

.hot-value {
  color: #f5576c;
  font-weight: 600;
  font-size: 0.9rem;
  white-space: nowrap;
}

.item-thumb {
  width: 60px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

/* 状态展示 */
.loading-state, .empty-state {
  text-align: center;
  padding: 60px 0;
  color: #888;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #eee;
  border-top-color: #f5576c;
  border-radius: 50%;
  animation: rotate 0.8s linear infinite;
  margin: 0 auto 15px;
}

.empty-emoji {
  font-size: 3rem;
  margin-bottom: 10px;
}

@keyframes rotate {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2.5rem;
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-title {
    justify-content: center;
  }

  .custom-tabs {
    margin: 10px 0;
  }

  .refresh-btn {
    justify-content: center;
    padding: 10px;
  }

  .item-title {
    font-size: 0.95rem;
  }

  .item-thumb {
    display: none;
  }

  .engine-chip {
    font-size: 0.8rem;
    padding: 5px 10px;
  }
}
</style>