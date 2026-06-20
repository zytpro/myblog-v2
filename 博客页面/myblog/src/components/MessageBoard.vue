<template>
  <div class="page-wrapper">
    <!-- 背景气泡动画 -->
    <ul class="bg-bubbles">
      <li v-for="n in 10" :key="n"></li>
    </ul>

    <div class="message-container">
      <!-- 头部标题 -->
      <div class="message-header">
        <h1 class="title">Message Board</h1>
        <p class="subtitle">寄托思念，留下祝福</p>
      </div>

      <!-- 弹幕区域 (重写逻辑) -->
      <div class="danmaku-wrapper" ref="danmakuContainer">

        <!-- 加载中 -->
        <div v-if="isLoading" class="loading-state">
          <div class="spinner"></div>
          <p>正在读取留言...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!displayDanmus.length" class="empty-state">
          <div class="empty-icon">📫</div>
          <div class="empty-text">还没有留言，快来抢沙发~</div>
        </div>

        <!-- 弹幕列表 -->
        <div
            v-for="item in displayDanmus"
            :key="item.id"
            class="danmu-item"
            :class="['track-' + item.track]"
            :style="item.style"
        >
          <div class="danmu-content">
            <span class="avatar" :style="{ backgroundColor: item.avatarColor }">{{ item.text.charAt(0) }}</span>
            <span class="text">{{ item.text }}</span>
          </div>
        </div>
      </div>

      <!-- 发送框区域 -->
      <div class="send-section">
        <div class="input-wrapper">
          <input
              v-model="newMessage"
              type="text"
              placeholder="说点好听的，万一实现了呢..."
              maxlength="100"
              @keyup.enter="sendDanmaku"
              :disabled="sending"
          >
          <button
              class="send-btn"
              @click="sendDanmaku"
              :disabled="!newMessage.trim() || sending"
          >
            <span v-if="!sending">发送</span>
            <span v-else class="sending-dots">...</span>
          </button>
        </div>
        <div class="input-tip">
          <span class="tip-text">💡 友善发言，传递正能量</span>
          <span class="char-count" :class="{ 'warning': newMessage.length > 80 }">
            {{ newMessage.length }}/100
          </span>
        </div>
      </div>

      <!-- 自定义提示条 -->
      <transition name="toast">
        <div v-if="showSnackbar" class="custom-toast" :class="snackbarType">
          <i class="icon">{{ snackbarIcon }}</i>
          <span>{{ snackbarMessage }}</span>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted, computed, nextTick} from 'vue';
import request from '../utills/request.js';

// --- 状态定义 ---
const newMessage = ref('');
const sending = ref(false);
const isLoading = ref(true);
const displayDanmus = ref([]); // 用于显示的弹幕对象数组
const danmakuContainer = ref(null);

// --- 弹幕逻辑配置 ---
const TRACK_COUNT = 6; // 轨道数量 (将屏幕高度分为几行)
const ANIMATION_DURATION_BASE = 15; // 基础滚动时间(秒)
const ANIMATION_DURATION_RANDOM = 10; // 随机增加的时间(秒)

// --- 提示框逻辑 ---
const showSnackbar = ref(false);
const snackbarMessage = ref('');
const snackbarType = ref('info');
const snackbarIcon = computed(() => {
  switch (snackbarType.value) {
    case 'success':
      return '✔';
    case 'error':
      return '✖';
    case 'warning':
      return '!';
    default:
      return 'ℹ';
  }
});
const showMessage = (msg, type = 'info') => {
  snackbarMessage.value = msg;
  snackbarType.value = type;
  showSnackbar.value = true;
  setTimeout(() => showSnackbar.value = false, 3000);
};

// --- 核心：处理弹幕数据 ---
const processDanmakuData = (rawData) => {
  // 清空现有数据
  displayDanmus.value = [];

  if (!Array.isArray(rawData)) return;

  const validData = rawData
      .map(item => typeof item === 'string' ? item : (item.message || ''))
      .filter(text => text.trim() !== '');

  // 为每条弹幕生成样式属性
  displayDanmus.value = validData.map((text, index) => {
    // 1. 随机分配轨道 (0 ~ TRACK_COUNT-1)
    // 为了避免相邻弹幕重叠，可以简单的轮询或者随机
    const track = index % TRACK_COUNT;

    // 2. 随机速度 (15s ~ 25s)
    const duration = ANIMATION_DURATION_BASE + Math.random() * ANIMATION_DURATION_RANDOM;

    // 3. 随机延迟 (0s ~ 15s)，让它们错开出现，不要一窝蜂出来
    // 第一屏的数据需要散开，后续新增的数据延迟可以小一点
    const delay = Math.random() * 20;

    // 4. 随机头像颜色
    const colors = ['#ff9a9e', '#a18cd1', '#fad0c4', '#ffecd2', '#84fab0', '#8fd3f4'];
    const avatarColor = colors[Math.floor(Math.random() * colors.length)];

    return {
      id: index + Date.now(), // 唯一ID
      text: text,
      track: track, // 用于决定 top 位置
      avatarColor: avatarColor,
      style: {
        animationDuration: `${duration}s`,
        animationDelay: `${delay}s`,
        top: `${(track * 12) + 5}%` // 动态计算 Top 值：例如 5%, 17%, 29%...
      }
    };
  });
};

// --- API ---
const getdanmus = async () => {
  try {
    const response = await request.get('/message');
    if (response.data) {
      processDanmakuData(response.data);
    }
  } catch (error) {
    console.error('获取弹幕失败', error);
  } finally {
    isLoading.value = false;
  }
};

const sendDanmaku = async () => {
  if (!newMessage.value.trim() || sending.value) return;

  sending.value = true;
  try {
    const currentTime = new Date().toISOString().slice(0, 19).replace('T', ' ');
    const requestData = {
      message: newMessage.value.trim(),
      createTime: currentTime,
      color: '#FFFFFF',
      act: 1
    };
    
    const response = await request.post('/message/add', requestData);
    
    if (response.code === 0) {
      // 发送成功后，手动将新弹幕加入到视图中
      const newDanmu = {
        id: Date.now(),
        text: newMessage.value.trim(),
        track: Math.floor(Math.random() * TRACK_COUNT), // 随机轨道
        avatarColor: '#ff9a9e',
        style: {
          animationDuration: `${ANIMATION_DURATION_BASE}s`,
          animationDelay: '0s', // 新发送的立即显示
          top: `${(Math.floor(Math.random() * TRACK_COUNT) * 12) + 5}%`
        }
      };
      displayDanmus.value.push(newDanmu);
      
      newMessage.value = '';
      showMessage("发送成功，已上墙！", 'success');
    } else {
      showMessage(response.message || '发送失败', 'error');
    }
  } catch (error) {
    showMessage('网络开小差了，稍后再试', 'error');
  } finally {
    sending.value = false;
  }
};

let refreshInterval = null;

onMounted(() => {
  getdanmus();
  // 每30秒刷新一次全量数据，或者你可以改为只追加
  refreshInterval = setInterval(getdanmus, 30000);
});

onUnmounted(() => {
  if (refreshInterval) clearInterval(refreshInterval);
});
</script>

<style scoped>
/* 引入字体 */
@import url('https://fonts.googleapis.com/css2?family=Fredoka:wght@400;600&family=Noto+Sans+SC:wght@400;700&display=swap');

.page-wrapper {
  min-height: 100vh;
  width: 100%;
  position: relative;
  font-family: 'Fredoka', 'Noto Sans SC', sans-serif;
  background-image: linear-gradient(120deg, #fccb90 0%, #d57eeb 100%);
  overflow: hidden;
}

/* 气泡背景动画保持不变 */
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

.message-container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.message-header {
  text-align: center;
  padding: 40px 0 20px;
  color: #fff;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 3.5rem;
  margin: 0;
  font-family: 'Fredoka', cursive;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-top: 10px;
  letter-spacing: 2px;
}

/* --- 弹幕区域核心样式 (修改部分) --- */
.danmaku-wrapper {
  flex: 1;
  position: relative;
  margin: 20px 0;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1); /* 轻微背景让区域可见 */
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden; /* 关键：隐藏超出范围的弹幕 */
  min-height: 400px;
  /* 确保有宽度 */
  width: 100%;
}

.danmu-item {
  position: absolute;
  /* 初始位置在屏幕最右侧之外 */
  left: 100%;
  /* 必须不换行 */
  white-space: nowrap;
  /* 关键动画：向左移动 */
  animation-name: moveLeft;
  animation-timing-function: linear;
  animation-iteration-count: infinite; /* 循环播放 */

  /* 鼠标悬停暂停 */
  cursor: pointer;
  z-index: 10;
}

.danmu-item:hover {
  animation-play-state: paused;
  z-index: 20; /* 悬停时置顶 */
}

/* 弹幕内容样式 */
.danmu-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.85); /* 胶囊背景 */
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}

.danmu-item:hover .danmu-content {
  background: #fff;
  transform: scale(1.1);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  font-weight: bold;
}

.text {
  color: #333;
  font-size: 15px;
  font-weight: 500;
}

/* 关键动画定义 */
@keyframes moveLeft {
  from {
    transform: translateX(0);
  }
  to {
    /* 移动到屏幕左侧之外，-100%是自身宽度，再减去视口宽度的一点冗余 */
    transform: translateX(calc(-100vw - 100%));
  }
}

/* 状态提示 */
.loading-state, .empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* --- 发送框样式 (保持一致) --- */
.send-section {
  width: 100%;
  max-width: 700px;
  margin: 0 auto 40px;
}

.input-wrapper {
  display: flex;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 50px;
  padding: 5px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.5);
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  background: #fff;
  transform: translateY(-2px);
  border-color: #fff;
}

.input-wrapper input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 15px 25px;
  font-size: 1.1rem;
  color: #555;
  outline: none;
}

.send-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 40px;
  padding: 0 35px;
  font-weight: 600;
  cursor: pointer;
  min-width: 100px;
  transition: all 0.3s;
  margin: 3px;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.input-tip {
  display: flex;
  justify-content: space-between;
  padding: 10px 20px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
}

/* Toast */
.custom-toast {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 12px 25px;
  border-radius: 50px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 9999;
  font-weight: 600;
  color: #555;
}

.custom-toast.success .icon {
  color: #4caf50;
}

.custom-toast.error .icon {
  color: #ff5252;
}

.toast-enter-active, .toast-leave-active {
  transition: all 0.4s;
}

.toast-enter-from, .toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -30px);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .title {
    font-size: 2.5rem;
  }

  .danmaku-wrapper {
    min-height: 300px;
  }

  .danmu-content {
    padding: 4px 12px;
    font-size: 13px;
  }

  .input-wrapper {
    flex-direction: column;
    border-radius: 20px;
    padding: 10px;
  }

  .send-btn {
    width: 100%;
    padding: 12px 0;
    border-radius: 15px;
    margin-top: 10px;
  }
}
</style>