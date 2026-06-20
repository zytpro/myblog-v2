<template>
  <teleport to="body">
    <div class="music-player-container">
      <div class="music-toggle-btn" :class="{ 'playing': isPlaying }" @click="togglePlayer">
        <v-icon size="24" color="white" :class="{ 'music-icon': isPlaying }">
          {{ isPlaying ? 'mdi-music-note' : 'mdi-music-note-off' }}
        </v-icon>
        <div class="breathing-light" v-if="isPlaying"></div>
      </div>
      
      <div class="music-player-modal" :class="{ 'show': isOpen }" v-show="isOpen">
        <div class="music-player-content">
          <div class="control-buttons">
            <div class="minimize-btn" @click="minimizePlayer">
              <v-icon size="16" color="white">mdi-minus</v-icon>
            </div>
            <div class="close-btn" @click="closePlayer">
              <v-icon size="16" color="white">mdi-close</v-icon>
            </div>
          </div>
          
          <div class="music-info" v-if="tracks.length > 0">
            <div class="music-cover" v-if="displayCoverUrl">
              <div
                class="cover-image-bg"
                :class="{ spinning: isPlaying }"
                :style="{ backgroundImage: 'url(' + displayCoverUrl + ')' }"
              ></div>
            </div>
            
            <div class="music-details">
              <h3 class="music-title">{{ currentTrack.title || '未命名歌曲' }}</h3>
              <p v-if="currentTrack.artist" class="music-artist">{{ currentTrack.artist }}</p>
            </div>
          </div>
          
          <div class="music-info" v-else>
            <div class="music-details">
              <h3 class="music-title">暂无音乐</h3>
              <p class="music-artist">请先上传音乐文件</p>
            </div>
          </div>
          
          <div class="progress-container" v-if="tracks.length > 0">
            <div class="time-display">
              <span class="current-time">{{ formatTime(currentTime) }}</span>
              <span class="total-time">{{ formatTime(duration) }}</span>
            </div>
            <div class="progress-bar" @click="seekTo">
              <div class="progress-bg"></div>
              <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
              <div class="progress-handle" :style="{ left: progressPercent + '%' }"></div>
            </div>
          </div>
          
          <div class="controls" v-if="tracks.length > 0">
            <v-btn
              icon
              variant="text"
              color="white"
              @click="previousTrack"
              class="control-btn"
            >
              <v-icon size="24">mdi-skip-previous</v-icon>
            </v-btn>
            
            <v-btn
              icon
              variant="text"
              color="white"
              @click="togglePlay"
              class="control-btn play-btn"
              :class="{ 'playing': isPlaying }"
            >
              <v-icon size="32">
                {{ isPlaying ? 'mdi-pause' : 'mdi-play' }}
              </v-icon>
            </v-btn>
            
            <v-btn
              icon
              variant="text"
              color="white"
              @click="nextTrack"
              class="control-btn"
            >
              <v-icon size="24">mdi-skip-next</v-icon>
            </v-btn>
          </div>
          
          <div class="volume-container" v-if="tracks.length > 0">
            <v-icon size="16" color="white" class="volume-icon">mdi-volume-high</v-icon>
            <div class="volume-slider" @click="setVolume">
              <div class="volume-bg"></div>
              <div class="volume-fill" :style="{ width: volumePercent + '%' }"></div>
              <div class="volume-handle" :style="{ left: volumePercent + '%' }"></div>
            </div>
          </div>
          
          <audio
            v-if="tracks.length > 0"
            ref="audioElement"
            :src="resolvedAudioSrc"
            preload="auto"
            crossorigin="anonymous"
            @loadedmetadata="onLoadedMetadata"
            @timeupdate="onTimeUpdate"
            @ended="onEnded"
            @error="onError"
          ></audio>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utills/request.js'
import { resolveMusicUrl } from '../utills/imageUtils.js'
import { MUSIC_BASE_URL } from '../config/env.js'

const isOpen = ref(false)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.7)
const audioElement = ref(null)
const playlistLoaded = ref(false)
const displayCoverUrl = ref('')

// 监听路由变化，自动关闭并暂停播放器，避免导航后“自己弹出”
const route = useRoute()
watch(() => route.fullPath, () => {
  isOpen.value = false
  if (audioElement.value) {
    audioElement.value.pause()
  }
  isPlaying.value = false
})

const tracks = ref([])

const currentTrackIndex = ref(0)
const currentTrack = computed(() => tracks.value[currentTrackIndex.value])

const progressPercent = computed(() => {
  return duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0
})

const volumePercent = computed(() => {
  return volume.value * 100
})

const resolveUrl = (raw) => {
  if (!raw) return ''
  const url = String(raw).trim()
  
  if (/^https?:\/\//i.test(url) || url.startsWith('data:')) {
    return url
  }
  
  const base = MUSIC_BASE_URL
  const result = (base ? base.replace(/\/$/, '') + '/' : '/') + url.replace(/^\//, '')
  
  return result
}

const resolvedCoverUrl = computed(() => {
  const raw = (currentTrack.value?.cover || '').trim()
  if (!raw) return ''
  const full = resolveUrl(raw)
  
  return full
})

const resolvedAudioSrc = computed(() => {
  const raw = (currentTrack.value?.src || '').trim()
  return resolveUrl(raw)
})

const togglePlayer = () => {
  if (!isOpen.value && !playlistLoaded.value) {
    fetchAndSetPlaylist()
  }
  isOpen.value = !isOpen.value
  
  if (!tracks.value.length) {
    return
  }
  
  const srcReady = Boolean(resolvedAudioSrc.value)
  if (!audioElement.value || !srcReady) {
    return
  }
  const currentSrc = audioElement.value.getAttribute('src') || ''
  if (currentSrc !== resolvedAudioSrc.value) {
    audioElement.value.setAttribute('src', resolvedAudioSrc.value)
    try { audioElement.value.load() } catch {}
  }
  if (isPlaying.value) {
    audioElement.value.pause()
    isPlaying.value = false
  } else {
    audioElement.value.play().then(() => {
      isPlaying.value = true
    }).catch((e) => {
      console.error('Play failed:', e)
      isPlaying.value = false
    })
  }
}

const closePlayer = () => {
  isOpen.value = false
}

const minimizePlayer = () => {
  isOpen.value = false
}

const togglePlay = () => {
  if (!tracks.value.length) {
    return
  }
  
  const srcReady = Boolean(resolvedAudioSrc.value)
  if (!audioElement.value || !srcReady) {
    return
  }
  const currentSrc = audioElement.value.getAttribute('src') || ''
  if (currentSrc !== resolvedAudioSrc.value) {
    audioElement.value.setAttribute('src', resolvedAudioSrc.value)
    try { audioElement.value.load() } catch {}
  }
  if (isPlaying.value) {
    audioElement.value.pause()
    isPlaying.value = false
  } else {
    audioElement.value.play().then(() => {
      isPlaying.value = true
    }).catch((e) => {
      console.error('Play failed:', e)
      isPlaying.value = false
    })
  }
}

const previousTrack = () => {
  currentTrackIndex.value = currentTrackIndex.value > 0 
    ? currentTrackIndex.value - 1 
    : tracks.value.length - 1
  loadTrack()
}

const nextTrack = () => {
  currentTrackIndex.value = currentTrackIndex.value < tracks.value.length - 1 
    ? currentTrackIndex.value + 1 
    : 0
  loadTrack()
}

const loadTrack = () => {
  if (audioElement.value) {
    const newSrc = resolvedAudioSrc.value
    if (newSrc && newSrc !== audioElement.value.src) {
      audioElement.value.src = newSrc
    }
    
    audioElement.value.load()
    if (isPlaying.value) {
      audioElement.value.play()
    }
  }
}

// 提供给外部调用：动态设置歌曲/封面 URL
const setTrack = ({ src, cover, title, artist }) => {
  const newSrc = resolveUrl(src)
  const newCover = resolveUrl(cover)
  
  tracks.value[0] = {
    id: Date.now(),
    title: title || currentTrack.value?.title || '',
    artist: artist || currentTrack.value?.artist || '',
    src: newSrc || currentTrack.value?.src || '',
    cover: newCover || currentTrack.value?.cover || ''
  }
  currentTrackIndex.value = 0
  isOpen.value = true
  isPlaying.value = false
  
  loadTrack()
}

const setByHash = (hash, meta = {}) => {
  const h = String(hash || '').trim()
  if (!h) return
  setTrack({
    src: `/file/preview/music/${h}.mp3`,
    cover: `/file/cover/music/${h}.png`,
    title: meta.title || h,
    artist: meta.artist || ''
  })
}

const setByFileName = (fileName, meta = {}) => {
  const f = String(fileName || '').trim()
  if (!f) return
  setTrack({
    src: `/file/preview/music/${f}`,
    cover: `/file/cover/music/${f}`,
    title: meta.title || f,
    artist: meta.artist || ''
  })
}

const setPlaylist = (list = []) => {
  const normalized = Array.isArray(list) ? list.map((t, idx) => ({
    id: t.id || Date.now() + idx,
    title: t.title || '',
    artist: t.artist || '',
    src: resolveUrl(t.src || ''),
    cover: resolveUrl(t.cover || '')
  })) : []
  if (normalized.length > 0) {
    tracks.value = normalized
    currentTrackIndex.value = 0
    isOpen.value = true
    isPlaying.value = false
    loadTrack()
    playlistLoaded.value = true
  }
}

const fetchAndSetPlaylist = async () => {
      try {
        const res = await request.get('/file/list', { params: { type: 'music' } })
        const data = Array.isArray(res?.data) ? res.data : []
        if (data.length === 0) {
          console.log('没有找到音乐文件')
          return
        }
        const list = data.map((file, idx) => ({
          id: idx + 1,
          title: file.fileName,
          artist: file.artist || '',
          src: `/file/preview/music/${file.storedFileName}`,
          cover: `/file/cover/music/${file.storedFileName}`
        }))
        setPlaylist(list)
      } catch (e) {
        console.error('获取音乐列表失败:', e)
      }
    }

defineExpose({ setTrack, setPlaylist, setByHash, setByFileName })

const seekTo = (event) => {
  const rect = event.currentTarget.getBoundingClientRect()
  const clickX = event.clientX - rect.left
  const percent = clickX / rect.width
  const newTime = percent * duration.value
  audioElement.value.currentTime = newTime
}

const setVolume = (event) => {
  const rect = event.currentTarget.getBoundingClientRect()
  const clickX = event.clientX - rect.left
  const percent = clickX / rect.width
  volume.value = Math.max(0, Math.min(1, percent))
  audioElement.value.volume = volume.value
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const onLoadedMetadata = () => {
  duration.value = audioElement.value.duration
  audioElement.value.volume = volume.value
}

const onTimeUpdate = () => {
  currentTime.value = audioElement.value.currentTime
}

const onEnded = () => {
  isPlaying.value = false
  nextTrack()
}

const onError = (error) => {
  console.error('Audio error:', error)
  isPlaying.value = false
}

onMounted(() => {
  if (audioElement.value) {
    audioElement.value.volume = volume.value
  }
  const tryLoad = (url) => {
    if (!url) { 
      displayCoverUrl.value = ''; 
      return 
    }
    const img = new Image()
    img.onload = () => { 
      displayCoverUrl.value = url 
    }
    img.onerror = () => { 
      displayCoverUrl.value = '' 
    }
    img.src = url
  }
  tryLoad(resolvedCoverUrl.value)
  watch(resolvedCoverUrl, (u) => tryLoad(u))
})

onUnmounted(() => {
  if (audioElement.value) {
    audioElement.value.pause()
  }
})
</script>

<style scoped>
.music-player-container {
  position: fixed;
  top: 100px;
  right: 20px;
  z-index: 99998;
  box-sizing: border-box;
}

.music-toggle-btn {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.3);
  outline: none !important;
  position: relative;
  overflow: hidden;
}

.music-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.music-toggle-btn:focus {
  outline: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2) !important;
}

.music-toggle-btn:focus-visible {
  outline: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2) !important;
}

.music-toggle-btn.playing {
  animation: pulse 2s ease-in-out infinite;
  border-color: rgba(255, 255, 255, 0.6);
}

.music-icon {
  animation: bounce 1s ease-in-out infinite;
}

/* 呼吸灯光效果 */
.breathing-light {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4, #45b7d1, #96ceb4);
  background-size: 400% 400%;
  animation: breathing 3s ease-in-out infinite, gradientShift 4s ease infinite;
  z-index: -1;
  opacity: 0.7;
}

/* 脉冲动画 */
@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-3px);
  }
  60% {
    transform: translateY(-1px);
  }
}

@keyframes breathing {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.music-player-modal {
  position: fixed;
  top: 64px;
  left: auto !important;
  right: -400px !important;
  width: 350px;
  height: calc(100vh - 64px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #f5576c 75%, #4facfe 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  backdrop-filter: blur(10px);
  transition: right 0.3s ease;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.2);
  border-left: 1px solid rgba(255, 255, 255, 0.2);
  border-top-left-radius: 20px;
  border-bottom-left-radius: 20px;
  z-index: 99999;
  pointer-events: auto;
}

.music-player-modal.show {
  right: 0 !important;
}

.music-player-content {
  padding: 30px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  border-top-left-radius: 20px;
  border-bottom-left-radius: 20px;
  pointer-events: auto;
}

.control-buttons {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 10px;
}

.minimize-btn,
.close-btn {
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none !important;
  border: none !important;
}

.minimize-btn:hover,
.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.minimize-btn:focus,
.close-btn:focus {
  outline: none !important;
  box-shadow: none !important;
}

.minimize-btn:focus-visible,
.close-btn:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

.minimize-btn:hover {
  background: rgba(255, 193, 7, 0.3) !important;
}

.close-btn:hover {
  background: rgba(244, 67, 54, 0.3) !important;
}

.music-info {
  margin-top: 80px;
  text-align: center;
  margin-bottom: 40px;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.music-cover {
  margin-bottom: 25px;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 160px;
  height: 160px;
  margin-left: auto;
  margin-right: auto;
  position: relative; /* Added for absolute positioning of placeholder */
  background: transparent !important;
}

.cover-image-bg {
  width: 160px !important;
  height: 160px !important;
  border-radius: 50% !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
  border: none;
  background-size: 120% 120%;
  background-position: center;
  background-repeat: no-repeat;
}

/* 旋转动画：播放时转动 */
@keyframes rotateCover {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spinning {
  animation: rotateCover 8s linear infinite;
}

/* deprecated img+placeholder styles kept for safety but not used */
.cover-image {
  border-radius: 50% !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  border: 3px solid rgba(255, 255, 255, 0.3);
  width: 120px !important;
  height: 120px !important;
  object-fit: cover;
  display: none;
  position: relative;
  z-index: 1;
}

.cover-placeholder { display: none; }

.music-title {
  font-size: 1.2rem;
  font-weight: 800;
  margin-bottom: 10px;
  line-height: 1.4;
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4, #45b7d1, #96ceb4, #feca57);
  background-size: 300% 300%;
  animation: gradientShift 6s ease infinite;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
}

.music-artist {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.progress-container {
  margin-bottom: 35px;
}

.time-display {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.8);
}

.progress-bar {
  position: relative;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  cursor: pointer;
  overflow: hidden;
}

.progress-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
}

.progress-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, #ff6b6b, #4ecdc4);
  border-radius: 3px;
  transition: width 0.1s ease;
}

.progress-handle {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: left 0.1s ease;
}

.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 25px;
  margin-bottom: 35px;
}

.control-btn {
  transition: all 0.3s ease;
  outline: none !important;
  border: none !important;
}

.control-btn:hover {
  transform: scale(1.1);
}

.control-btn:focus {
  outline: none !important;
  box-shadow: none !important;
}

.control-btn:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

.play-btn {
  width: 60px !important;
  height: 60px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 50% !important;
  outline: none !important;
  border: none !important;
  transition: all 0.3s ease;
}

.play-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: scale(1.1);
}

.play-btn:focus {
  outline: none !important;
  box-shadow: none !important;
}

.play-btn:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

.play-btn.playing {
  animation: playPulse 1.5s ease-in-out infinite;
}

@keyframes playPulse {
  0%, 100% {
    transform: scale(1);
    background: rgba(255, 255, 255, 0.2) !important;
  }
  50% {
    transform: scale(1.05);
    background: rgba(255, 255, 255, 0.4) !important;
  }
}

.volume-container {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: auto;
}

.volume-icon {
  flex-shrink: 0;
}

.volume-slider {
  flex: 1;
  position: relative;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  cursor: pointer;
  overflow: hidden;
}

.volume-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
}

.volume-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: white;
  border-radius: 2px;
  transition: width 0.1s ease;
}

.volume-handle {
  position: absolute;
  top: 50%;
  width: 10px;
  height: 10px;
  background: white;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  transition: left 0.1s ease;
}

@keyframes gradientShift {
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

@media (max-width: 768px) {
  .music-player-modal {
    width: 100%;
    left: auto !important;
    right: -100% !important;
    top: 56px;
    height: calc(100vh - 56px);
    border-radius: 0;
  }
  
  .music-player-content {
    border-radius: 0;
  }
  
  .music-toggle-btn {
    width: 45px;
    height: 45px;
  }
}
</style> 