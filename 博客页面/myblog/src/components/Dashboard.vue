<template>
  <div class="dashboard-3d-container">
    <div class="animated-bg-layer"></div>
    <div class="floating-shapes">
      <div class="shape s1"></div>
      <div class="shape s2"></div>
      <div class="shape s3"></div>
    </div>

    <div class="dashboard-content">
      <!-- 欢迎横幅 -->
      <div class="welcome-section">
        <div class="welcome-card-3d">
          <div class="card-glass">
            <div class="welcome-info">
              <h1 class="animate-title">Hi, {{ userInfo.username || '管理员' }} 👋</h1>
              <p class="animate-subtitle">今天是 {{ currentDate }}，系统运行平稳。</p>
            </div>
            <div class="welcome-stats">
              <div class="stat-box">
                <span class="label">今日访问</span>
                <span class="num">{{ statistics.visitors }}</span>
              </div>
              <div class="stat-box">
                <span class="label">文章总数</span>
                <span class="num">{{ statistics.articles }}</span>
              </div>
              <div class="stat-box">
                <span class="label">评论互动</span>
                <span class="num">{{ statistics.comments }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 3D 数据卡片 -->
      <div class="cards-grid">
        <div class="card-3d-wrapper" @click="showDetail('visitors')">
          <div class="card-3d-inner color-blue">
            <div class="card-front">
              <div class="icon-layer">
                <el-icon>
                  <User/>
                </el-icon>
              </div>
              <div class="data-layer">
                <div class="card-title">总访问量</div>
                <div class="card-count">
                  <el-skeleton v-if="loading" :rows="1" animated class="skeleton-fix"/>
                  <span v-else>{{ statistics.visitors }}</span>
                </div>
                <div class="card-trend">
                  <el-icon>
                    <Top/>
                  </el-icon>
                  +12% 同比增长
                </div>
              </div>
              <div class="shine-effect"></div>
            </div>
          </div>
        </div>

        <div class="card-3d-wrapper" @click="showDetail('articles')">
          <div class="card-3d-inner color-purple">
            <div class="card-front">
              <div class="icon-layer">
                <el-icon>
                  <Document/>
                </el-icon>
              </div>
              <div class="data-layer">
                <div class="card-title">文章发布</div>
                <div class="card-count">
                  <el-skeleton v-if="loading" :rows="1" animated class="skeleton-fix"/>
                  <span v-else>{{ statistics.articles }}</span>
                </div>
                <div class="card-trend">
                  <el-icon>
                    <Top/>
                  </el-icon>
                  +5% 本周新增
                </div>
              </div>
              <div class="shine-effect"></div>
            </div>
          </div>
        </div>

        <div class="card-3d-wrapper" @click="showDetail('comments')">
          <div class="card-3d-inner color-cyan">
            <div class="card-front">
              <div class="icon-layer">
                <el-icon>
                  <ChatDotRound/>
                </el-icon>
              </div>
              <div class="data-layer">
                <div class="card-title">评论留言</div>
                <div class="card-count">
                  <el-skeleton v-if="loading" :rows="1" animated class="skeleton-fix"/>
                  <span v-else>{{ statistics.comments }}</span>
                </div>
                <div class="card-trend">
                  <el-icon>
                    <Top/>
                  </el-icon>
                  活跃互动
                </div>
              </div>
              <div class="shine-effect"></div>
            </div>
          </div>
        </div>

        <div class="card-3d-wrapper" @click="showDetail('system')">
          <div class="card-3d-inner color-green">
            <div class="card-front">
              <div class="icon-layer">
                <el-icon>
                  <Monitor/>
                </el-icon>
              </div>
              <div class="data-layer">
                <div class="card-title">系统健康度</div>
                <div class="card-count">100%</div>
                <div class="card-trend">
                  <div class="status-indicator"></div>
                  运行正常
                </div>
              </div>
              <div class="shine-effect"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-row">
        <div class="chart-box-3d glass-panel main-chart">
          <div class="panel-header">
            <h3>访问流量趋势</h3>
            <div class="controls">
              <el-radio-group v-model="visitTimeRange" size="small">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
              <el-button circle size="small" @click="refreshData">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="chart-body">
            <LineChart :data="visitData" />
          </div>
        </div>

        <div class="chart-box-3d glass-panel sub-chart">
          <div class="panel-header">
            <h3>内容构成</h3>
          </div>
          <div class="chart-body">
            <el-skeleton v-if="categoryLoading" :rows="5" animated/>
            <CategoryGlobe v-else :data="categoryData"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, Document, ChatDotRound, Monitor, Top, Refresh
} from '@element-plus/icons-vue'
import LineChart from './charts/LineChart.vue'
import CategoryGlobe from './charts/CategoryGlobe.vue'
import request from '../utills/request.js'

const userInfo = ref({
  username: '',
  avatar: '',
  email: '',
  phone: '',
  description: ''
})

const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  })
})

const statistics = reactive({
  visitors: 0,
  articles: 0,
  comments: 0,
})

const loading = ref(false)
const categoryLoading = ref(false)
let refreshTimer = null
const visitTimeRange = ref('week')
const categoryLegend = ref([])

const visitData = ref({
  grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
  tooltip: {trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)'},
  xAxis: {type: 'category', data: [], axisLine: {show: false}, axisTick: {show: false}},
  yAxis: {type: 'value', splitLine: {lineStyle: {type: 'dashed', color: '#eee'}}},
  series: [{
    type: 'line',
    smooth: true,
    showSymbol: false,
    data: [],
    itemStyle: {color: '#6366f1'},
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{offset: 0, color: 'rgba(99, 102, 241, 0.5)'}, {offset: 1, color: 'rgba(99, 102, 241, 0.01)'}]
      }
    }
  }]
})

const categoryData = ref({
  tooltip: {trigger: 'item'},
  legend: {bottom: '0%', left: 'center'},
  series: [{
    name: '分类',
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: {borderRadius: 10, borderColor: '#fff', borderWidth: 2},
    label: {show: false},
    labelLine: {show: false},
    data: []
  }]
})

const getUserInfo = async () => {
  try {
    const res = await request.post('/user/getPersonalInfo')
    if (res.code === 0) userInfo.value = res.data
  } catch (e) {
  }
}

const fetchStatistics = async () => {
  try {
    loading.value = true
    const res = await request.get('/dashboard')
    const data = res.data || res
    if (data) {
      statistics.visitors = Number(data.visitor) || 0
      statistics.articles = Number(data.articleNumber) || 0
      statistics.comments = Number(data.commentNumber) || 0
    }
  } catch (e) {
    statistics.visitors = 0
  } finally {
    loading.value = false
  }
}

const fetchCategoryData = async () => {
  try {
    categoryLoading.value = true
    const res = await request.get('/categories')
    if (Array.isArray(res)) {
      const activeCats = res.filter(i => i.status === '1')
      categoryData.value.series[0].data = activeCats.map(i => ({name: i.name, value: 1}))
    }
  } catch (e) {
  } finally {
    categoryLoading.value = false
  }
}

const fetchVisitData = async () => {
  try {
    const res = await request.get('/visitor/count')
    if (Array.isArray(res)) {
      const map = {}
      res.forEach(i => map[i.visit_date] = (map[i.visit_date] || 0) + 1)

      const dates = [], counts = []
      const days = visitTimeRange.value === 'week' ? 7 : 30
      const now = new Date()

      for (let i = days - 1; i >= 0; i--) {
        const d = new Date()
        d.setDate(now.getDate() - i)
        const dateStr = d.toISOString().split('T')[0]
        dates.push(dateStr.slice(5))
        counts.push(map[dateStr] || 0)
      }
      visitData.value.xAxis.data = dates
      visitData.value.series[0].data = counts
    }
  } catch (e) {
  }
}

const refreshData = () => {
  fetchStatistics()
  fetchCategoryData()
  fetchVisitData()
}

const showDetail = (type) => {
}

onMounted(() => {
  getUserInfo()
  refreshData()
  refreshTimer = setInterval(refreshData, 300000)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

watch(visitTimeRange, fetchVisitData)
</script>

<style scoped>
.dashboard-3d-container {
  min-height: 100vh;
  padding: 24px;
  position: relative;
  background: #f0f2f5;
  overflow-x: hidden;
  perspective: 1200px; /* 3D 视角深度 */
}

/* 动态流体背景 */
.animated-bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  z-index: 0;
  opacity: 0.6;
}

.floating-shapes .shape {
  position: absolute;
  filter: blur(50px);
  z-index: 0;
  opacity: 0.6;
  animation: float 10s infinite alternate;
}

.s1 {
  top: 10%;
  left: 10%;
  width: 300px;
  height: 300px;
  background: #a18cd1;
  border-radius: 40% 60% 70% 30%;
}

.s2 {
  bottom: 20%;
  right: 10%;
  width: 400px;
  height: 400px;
  background: #fbc2eb;
  border-radius: 60% 40% 30% 70%;
  animation-duration: 15s;
}

.s3 {
  top: 40%;
  left: 40%;
  width: 200px;
  height: 200px;
  background: #8fd3f4;
  border-radius: 50%;
  animation-duration: 12s;
}

@keyframes float {
  from {
    transform: translate(0, 0) rotate(0deg);
  }
  to {
    transform: translate(30px, 50px) rotate(10deg);
  }
}

.dashboard-content {
  position: relative;
  z-index: 1;
  max-width: 1400px;
  margin: 0 auto;
}

/* 欢迎横幅 3D 效果 */
.welcome-section {
  margin-bottom: 40px;
  transform-style: preserve-3d;
}

.welcome-card-3d {
  transition: transform 0.3s;
}

.card-glass {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  padding: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  transform: translateZ(20px); /* 3D 悬浮 */
}

.animate-title {
  font-size: 2rem;
  background: linear-gradient(45deg, #2d3436, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10px;
  font-weight: 800;
}

.welcome-stats {
  display: flex;
  gap: 30px;
}

.stat-box {
  text-align: center;
  background: rgba(255, 255, 255, 0.5);
  padding: 15px 25px;
  border-radius: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s;
}

.stat-box:hover {
  transform: translateY(-5px);
}

.stat-box .num {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
}

.stat-box .label {
  font-size: 0.9rem;
  color: #666;
}

/* 3D 卡片网格 */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.card-3d-wrapper {
  height: 180px;
  perspective: 1000px;
  cursor: pointer;
}

.card-3d-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  transform-style: preserve-3d;
  border-radius: 24px;
}

.card-3d-wrapper:hover .card-3d-inner {
  transform: rotateX(10deg) rotateY(10deg) scale(1.02);
  box-shadow: 20px 20px 60px rgba(0, 0, 0, 0.1);
}

.card-front {
  position: absolute;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

/* 颜色主题 */
.color-blue .icon-layer {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.color-purple .icon-layer {
  background: linear-gradient(135deg, #f093fb, #f5576c);
}

.color-cyan .icon-layer {
  background: linear-gradient(135deg, #89f7fe, #66a6ff);
}

.color-green .icon-layer {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
}

.icon-layer {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transform: translateZ(30px); /* 图标悬浮 */
}

.data-layer {
  transform: translateZ(20px); /* 文字悬浮 */
}

.card-title {
  font-size: 14px;
  color: #888;
  margin-bottom: 5px;
}

.card-count {
  font-size: 32px;
  font-weight: 800;
  color: #2d3436;
  margin-bottom: 5px;
}

.card-trend {
  font-size: 13px;
  color: #4caf50;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  background: #4caf50;
  border-radius: 50%;
  box-shadow: 0 0 10px #4caf50;
}

/* 扫光动画 */
.shine-effect {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(to right, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.4) 50%, rgba(255, 255, 255, 0) 100%);
  transform: skewX(-25deg);
  transition: 0.5s;
  pointer-events: none;
}

.card-3d-wrapper:hover .shine-effect {
  left: 150%;
  transition: 0.7s;
}

/* 图表布局 */
.charts-row {
  display: flex;
  gap: 24px;
}

.chart-box-3d {
  border-radius: 24px;
  padding: 24px;
  transition: transform 0.3s;
}

.chart-box-3d:hover {
  transform: translateY(-5px);
}

.glass-panel {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.main-chart {
  flex: 2;
  height: 420px;
}

.sub-chart {
  flex: 1;
  height: 420px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header h3 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.chart-body {
  height: 340px;
}

.skeleton-fix {
  width: 60px;
  height: 30px;
}

@media (max-width: 1200px) {
  .cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    flex-direction: column;
  }
}
@media (max-width: 768px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }

  .welcome-card-3d .card-glass {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .welcome-stats {
    width: 100%;
    justify-content: space-between;
  }
}
</style>