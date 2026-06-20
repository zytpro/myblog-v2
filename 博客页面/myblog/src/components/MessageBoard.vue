<template>
  <div class="danmaku-page">
    <!-- 温柔渐变背景 -->
    <div class="bg-blob bg-blob-1"></div>
    <div class="bg-blob bg-blob-2"></div>
    <div class="bg-blob bg-blob-3"></div>

    <div class="danmaku-layout">
      <div class="danmaku-header">
        <h1 class="header-title">留言弹幕墙</h1>
        <p class="header-sub">写下你的话，让它随风飘过 ✨</p>
      </div>

      <div class="danmaku-stage">
        <div v-if="isLoading" class="stage-state">
          <div class="dot-flashing"></div>
          <p>正在加载...</p>
        </div>
        <div v-else-if="!danmuList.length" class="stage-state">
          <p>还没有弹幕，快来写第一条吧 💌</p>
        </div>
        <div
          v-for="item in danmuList"
          :key="item.id"
          class="danmu-bullet"
          :style="item.style"
          @mouseenter="item.style.animationPlayState = 'paused'"
          @mouseleave="item.style.animationPlayState = 'running'"
        >
          <span class="bullet-text">{{ item.text }}</span>
        </div>
      </div>

      <div class="send-bar">
        <div class="send-box">
          <input
            v-model="newMessage"
            class="send-input"
            placeholder="说点什么吧..."
            maxlength="100"
            @keyup.enter="sendDanmaku"
            :disabled="sending"
          />
          <button class="send-btn" @click="sendDanmaku" :disabled="!newMessage.trim() || sending">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <transition name="toast">
      <div v-if="showSnackbar" class="custom-toast" :class="snackbarType">{{ snackbarMessage }}</div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import request from '../utills/request.js'

const TRACK_COUNT = 7

const newMessage = ref('')
const sending = ref(false)
const isLoading = ref(true)
const danmuList = ref([])
const showSnackbar = ref(false)
const snackbarMessage = ref('')
const snackbarType = ref('info')

const showMessage = (msg, type = 'info') => {
  snackbarMessage.value = msg
  snackbarType.value = type
  showSnackbar.value = true
  setTimeout(() => showSnackbar.value = false, 3000)
}

const hashStr = s => {
  let h = 0
  for (let i = 0; i < s.length; i++) h = ((h << 5) - h + s.charCodeAt(i)) | 0
  return Math.abs(h)
}

const stableId = (text, idx) => `dm-${hashStr(text)}-${idx}`

const makeBullet = (text, idx, existingTrack = null) => {
  const hash = hashStr(idx)
  const track = existingTrack !== null ? existingTrack : hash % TRACK_COUNT
  const duration = 14 + (hash % 16)
  const negativeDelay = -(hash % duration)

  return reactive({
    id: stableId(text, idx),
    text,
    track,
    style: {
      '--dur': `${duration}s`,
      '--delay': `${negativeDelay}s`,
      top: `${track * 13 + 2}%`,
      animationPlayState: 'running',
    },
  })
}

const getMessages = async () => {
  try {
    const res = await request.get('/message')
    let raw = []
    if (Array.isArray(res.data)) raw = res.data
    else if (Array.isArray(res)) raw = res

    const texts = raw
      .map(item => typeof item === 'string' ? item : (item.message || ''))
      .filter(t => t.trim())

    const newIds = new Set(texts.map((t, i) => stableId(t, i)))
    danmuList.value = danmuList.value.filter(item => newIds.has(item.id))

    const existingIds = new Set(danmuList.value.map(item => item.id))
    texts.forEach((t, i) => {
      if (!existingIds.has(stableId(t, i))) {
        danmuList.value.push(makeBullet(t, stableId(t, i)))
      }
    })
  } catch {
    // ignore
  } finally {
    isLoading.value = false
  }
}

const sendDanmaku = async () => {
  const text = newMessage.value.trim()
  if (!text || sending.value) return

  sending.value = true
  try {
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    const res = await request.post('/message/add', {
      message: text,
      createTime: now,
      color: '#FFFFFF',
      act: 1,
    })

    if (res.code === 0) {
      const counts = Array.from({ length: TRACK_COUNT }, () => 0)
      danmuList.value.forEach(item => { counts[item.track]++ })
      let best = 0
      for (let i = 1; i < TRACK_COUNT; i++) { if (counts[i] < counts[best]) best = i }

      danmuList.value.push(makeBullet(text, `new-${Date.now()}`, best))
      newMessage.value = ''
      showMessage('发送成功 ✨', 'success')
    } else {
      showMessage(res.message || '发送失败', 'error')
    }
  } catch {
    showMessage('网络开小差了', 'error')
  } finally {
    sending.value = false
  }
}

let refreshTimer = null

onMounted(() => {
  getMessages()
  refreshTimer = setInterval(getMessages, 30000)
})

onUnmounted(() => {
  clearInterval(refreshTimer)
})
</script>

<style scoped>
.danmaku-page {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(170deg, #fce4ec 0%, #f3e5f5 25%, #e8eaf6 50%, #e1f5fe 75%, #fce4ec 100%);
  background-size: 400% 400%;
  animation: bgShift 20s ease-in-out infinite;
  overflow: hidden;
  display: flex;
  justify-content: center;
}

@keyframes bgShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* ---- 漂浮色块 ---- */
.bg-blob {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.25;
  pointer-events: none;
}
.bg-blob-1 {
  width: 350px; height: 350px;
  background: #f8bbd0;
  top: -80px; right: -60px;
  animation: blobA 10s ease-in-out infinite;
}
.bg-blob-2 {
  width: 280px; height: 280px;
  background: #b3e5fc;
  bottom: -60px; left: -40px;
  animation: blobB 12s ease-in-out infinite;
}
.bg-blob-3 {
  width: 220px; height: 220px;
  background: #c5e1a5;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  animation: blobC 14s ease-in-out infinite;
}
@keyframes blobA {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-30px, 40px) scale(1.15); }
}
@keyframes blobB {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(40px, -20px) scale(1.1); }
}
@keyframes blobC {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.2); }
}

/* ---- 布局 ---- */
.danmaku-layout {
  position: relative; z-index: 1; width: 100%; max-width: 900px;
  display: flex; flex-direction: column; min-height: 100vh; padding: 20px;
}

/* ---- 头部 ---- */
.danmaku-header {
  text-align: center; padding: 40px 0 14px;
}
.header-title {
  font-size: 2rem; font-weight: 700; margin: 0 0 8px;
  background: linear-gradient(135deg, #e91e63, #9c27b0, #3f51b5);
  -webkit-background-clip: text; background-clip: text;
  color: transparent;
}
.header-sub {
  font-size: 0.9rem; color: rgba(0,0,0,0.3); margin: 0;
}

/* ---- 舞台 ---- */
.danmaku-stage {
  flex: 1; position: relative; margin: 10px 0 90px;
  border-radius: 24px;
  background: rgba(255,255,255,0.25);
  border: 1px solid rgba(255,255,255,0.4);
  backdrop-filter: blur(10px);
  overflow: hidden; min-height: 420px;
}

.stage-state {
  position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: rgba(0,0,0,0.25);
}

/* ---- 弹幕条 ---- */
.danmu-bullet {
  position: absolute;
  left: 100%;
  white-space: nowrap;
  padding: 7px 20px;
  border-radius: 20px;
  background: rgba(255,255,255,0.65);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255,255,255,0.7);
  box-shadow: 0 2px 10px rgba(0,0,0,0.04);
  cursor: default;
  transition: box-shadow 0.25s, background 0.25s, transform 0.25s;
  animation: bulletFly var(--dur, 18s) linear var(--delay, 0s) infinite;
}

.danmu-bullet:hover {
  background: rgba(255,255,255,0.9);
  border-color: rgba(255,255,255,0.95);
  box-shadow: 0 4px 20px rgba(233, 30, 99, 0.12), 0 0 30px rgba(156, 39, 176, 0.08);
  transform: scale(1.04);
}

.bullet-text {
  font-size: 14px;
  color: #555;
  font-weight: 500;
}

@keyframes bulletFly {
  from { transform: translateX(0); }
  to   { transform: translateX(calc(-100vw - 100%)); }
}

/* ---- 加载 ---- */
.dot-flashing {
  position: relative;
  width: 8px; height: 8px;
  border-radius: 50%;
  background: rgba(156, 39, 176, 0.4);
  animation: dotFlashing 1s infinite linear alternate;
  animation-delay: 0.5s;
}
.dot-flashing::before, .dot-flashing::after {
  content: ''; position: absolute; top: 0; width: 8px; height: 8px;
  border-radius: 50%; background: rgba(156, 39, 176, 0.4);
}
.dot-flashing::before { left: -15px; animation: dotFlashing 1s infinite alternate; animation-delay: 0s; }
.dot-flashing::after { left: 15px; animation: dotFlashing 1s infinite alternate; animation-delay: 1s; }
@keyframes dotFlashing {
  0% { background: rgba(156, 39, 176, 0.25); }
  100% { background: rgba(156, 39, 176, 0.5); }
}

/* ---- 发送栏 ---- */
.send-bar {
  position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%);
  z-index: 10; width: 90%; max-width: 520px;
}
.send-box {
  display: flex;
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.5);
  border-radius: 28px; overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
  transition: border-color 0.3s, box-shadow 0.3s;
}
.send-box:focus-within {
  border-color: rgba(233, 30, 99, 0.2);
  box-shadow: 0 4px 24px rgba(233, 30, 99, 0.08);
}
.send-input {
  flex: 1; background: transparent; border: none; padding: 13px 20px;
  font-size: 14px; color: #555; outline: none; font-family: inherit;
}
.send-input::placeholder { color: rgba(0,0,0,0.2); }
.send-btn {
  width: 44px; flex-shrink: 0; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #f48fb1, #ce93d8); border: none;
  color: #fff; cursor: pointer; transition: all 0.3s;
}
.send-btn:hover:not(:disabled) { background: linear-gradient(135deg, #f06292, #ba68c8); }
.send-btn:active:not(:disabled) { transform: scale(0.92); }
.send-btn:disabled { background: rgba(0,0,0,0.05); color: rgba(0,0,0,0.15); cursor: not-allowed; }

/* ---- Toast ---- */
.custom-toast {
  position: fixed; top: 28px; left: 50%; transform: translateX(-50%);
  padding: 9px 22px; border-radius: 20px; font-size: 13px; font-weight: 600; z-index: 9999;
  background: rgba(255,255,255,0.9); backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.6); color: #666;
}
.toast-enter-active, .toast-leave-active { transition: all 0.35s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, -20px); }

@media (max-width: 768px) {
  .header-title { font-size: 1.5rem; }
  .danmaku-stage { min-height: 300px; }
  .danmu-bullet { padding: 5px 14px; }
  .bullet-text { font-size: 13px; }
  .send-bar { width: 95%; bottom: 12px; }
}
</style>
