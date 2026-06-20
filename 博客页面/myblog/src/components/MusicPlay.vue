<template>
  <div class="music-player-wrapper">
    <!-- 动态模糊背景 -->
    <div class="dynamic-bg" :style="{ backgroundImage: `url(${coverUrl})` }"></div>
    <div class="bg-overlay"></div>

    <!-- 主体卡片 -->
    <div class="glass-card">

      <!-- 设置按钮 -->
      <div class="settings-trigger" :class="{ active: showSettings }">
        <i class="fas fa-cog" @click="toggleSettings"></i>
      </div>

      <!-- 设置面板 -->
      <transition name="fade-slide">
        <div v-show="showSettings" class="settings-panel">
          <div class="panel-header">
            <h3>个性化设置</h3>
            <span class="close-btn" @click="showSettings = false">&times;</span>
          </div>

          <div class="panel-body">
            <div class="setting-group">
              <label>界面主题</label>
              <div class="theme-grid">
                <div
                    v-for="color in themeColors"
                    :key="color.id"
                    class="theme-dot"
                    :class="{ active: currentTheme === color.id }"
                    :style="{ background: color.gradient }"
                    @click="setTheme(color)"
                ></div>
              </div>
            </div>

            <div class="setting-group">
              <label>歌词颜色</label>
              <div class="color-pickers">
                <div class="color-item">
                  <span>普通</span>
                  <input type="color" v-model="lyricColor" @change="updateStyles">
                </div>
                <div class="color-item">
                  <span>高亮</span>
                  <input type="color" v-model="activeLyricColor" @change="updateStyles">
                </div>
              </div>
            </div>

            <div class="setting-group">
              <div class="range-header">
                <label>字体大小</label>
                <span>{{ lyricFontSize }}px</span>
              </div>
              <input
                  type="range"
                  class="styled-range"
                  v-model.number="lyricFontSize"
                  min="12"
                  max="32"
                  @change="updateStyles"
              >
            </div>

            <div class="setting-group">
              <label>歌词对齐</label>
              <div class="align-options">
                <button :class="{active: lyricAlign === 'left'}" @click="changeAlign('left')"><i
                    class="fas fa-align-left"></i></button>
                <button :class="{active: lyricAlign === 'center'}" @click="changeAlign('center')"><i
                    class="fas fa-align-center"></i></button>
                <button :class="{active: lyricAlign === 'right'}" @click="changeAlign('right')"><i
                    class="fas fa-align-right"></i></button>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <!-- 播放器内容区 -->
      <div class="player-content">
        <!-- 左侧：封面与信息 -->
        <div class="left-section">
          <div class="cover-wrapper">
            <div class="cover-glow" :class="{ 'playing': isPlaying }"></div>
            <img
                :src="coverUrl || defaultCover"
                alt="封面"
                class="cover-image"
                :class="{ 'rotate': isPlaying }"
            />
            <!-- 唱片中心孔 -->
            <div class="disc-hole"></div>
          </div>

          <div class="track-info">
            <h2 class="title" :title="musicName">{{ musicName || '未知歌曲' }}</h2>
            <p class="artist">{{ artist || '未知歌手' }}</p>
          </div>
        </div>

        <!-- 右侧：歌词 -->
        <div class="right-section">
          <div class="lyrics-mask">
            <div class="lyrics-scroll" ref="lyricContainer">
              <div class="padding-space"></div>
              <p
                  v-for="(line, index) in lyrics"
                  :key="index"
                  :class="{
                  'active': index === currentLyricIndex,
                  'near': Math.abs(index - currentLyricIndex) <= 1 
                }"
                  class="lyric-line"
                  ref="lyricLines"
                  @click="seekToLyric(line.time)"
              >
                {{ line.text }}
              </p>
              <div v-if="lyrics.length === 0" class="no-lyrics">暂无歌词</div>
              <div class="padding-space"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部控制栏 -->
      <div class="control-bar">
        <!-- 进度条 -->
        <div class="progress-area">
          <span class="time-text">{{ formatTime(progress) }}</span>
          <div class="progress-track" @click="handleProgressClick" ref="progressBar">
            <div class="progress-fill" :style="{ width: `${(progress / duration) * 100}%` }">
              <div class="progress-thumb"></div>
            </div>
          </div>
          <span class="time-text">{{ formatTime(duration) }}</span>
        </div>

        <!-- 按钮组 -->
        <div class="controls-row">
          <div class="buttons-left">
            <button class="icon-btn mode-btn" @click="togglePlayMode"
                    :title="playMode === 'loop' ? '单曲循环' : '顺序播放'">
              <i :class="playMode === 'loop' ? 'fas fa-redo-alt' : 'fas fa-random'"></i>
            </button>
          </div>

          <div class="buttons-center">
            <button class="icon-btn prev-btn" title="上一首(模拟)">
              <i class="fas fa-step-backward"></i>
            </button>
            <button class="play-fab" @click="togglePlay">
              <i :class="isPlaying ? 'fas fa-pause' : 'fas fa-play'"></i>
            </button>
            <button class="icon-btn next-btn" title="下一首(模拟)">
              <i class="fas fa-step-forward"></i>
            </button>
          </div>

          <div class="buttons-right">
            <div class="volume-wrapper" @mouseenter="showVolume = true" @mouseleave="showVolume = false">
              <i
                  class="fas"
                  :class="volume === 0 ? 'fa-volume-mute' : volume < 0.5 ? 'fa-volume-down' : 'fa-volume-up'"
                  @click="toggleMute"
              ></i>
              <div class="volume-slider-container">
                <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.01"
                    v-model="volume"
                    @input="adjustVolume"
                    class="volume-range"
                    :style="{ backgroundSize: `${volume * 100}% 100%` }"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <audio
        ref="audio"
        :src="musicUrl"
        @play="onPlay"
        @pause="onPause"
        @timeupdate="onTimeUpdate"
        @ended="onEnded"
        :loop="playMode === 'loop'"
        autoplay
        crossorigin="anonymous"
      />
    </div>
  </div>
</template>

<script>
import request from "../utills/request.js";

export default {
  data() {
    return {
      defaultCover: 'https://via.placeholder.com/300/1e2024/ffffff?text=Music',
      coverUrl: "",
      musicUrl: "",
      musicName: "",
      artist: "",
      isPlaying: false,
      progress: 0,
      duration: 0,
      volume: 1,
      lyrics: [],
      currentLyricIndex: -1,
      isInitialPlay: true,
      playMode: 'sequential',
      previousVolume: 1,
      showSettings: false,
      showVolume: false,
      lyricColor: '#e0e0e0',
      activeLyricColor: '#ffffff',
      lyricFontSize: 18,
      lyricAlign: 'center',
      currentTheme: 'default',
      themeColors: [
        { 
          id: 'default',
          gradient: 'linear-gradient(135deg, #1e2024 0%, #232526 100%)',
          accent: 'linear-gradient(90deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)'
        },
        {
          id: 'ocean',
          gradient: 'linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%)',
          accent: 'linear-gradient(to right, #4facfe 0%, #00f2fe 100%)'
        },
        { 
          id: 'purple', 
          gradient: 'linear-gradient(135deg, #2d1b4e 0%, #1a1033 100%)',
          accent: 'linear-gradient(to right, #b24592 0%, #f15f79 100%)'
        },
        {
          id: 'forest',
          gradient: 'linear-gradient(135deg, #134e5e 0%, #71b280 100%)',
          accent: 'linear-gradient(to right, #d4fc79 0%, #96e6a1 100%)'
        }
      ]
    };
  },
  created() {
    const urlParams = new URLSearchParams(window.location.search);
    this.coverUrl = urlParams.get("coverUrl");
    this.musicUrl = urlParams.get("musicUrl");
    this.musicName = urlParams.get("name");
    this.artist = urlParams.get("artist") || "Unknown Artist";

    if (this.musicName) {
      this.loadLyrics(this.musicName);
    }

    this.$nextTick(() => {
      const audio = this.$refs.audio;
      if (audio) {
        audio.volume = this.volume;
        const playPromise = audio.play();
        if (playPromise !== undefined) {
          playPromise.catch(() => console.log("等待用户交互后播放"));
        }
      }
    });
  },
  methods: {
    togglePlay() {
      const audio = this.$refs.audio;
      if (this.isPlaying) {
        audio.pause();
      } else {
        audio.play();
      }
    },
    onPlay() {
      this.isPlaying = true;
      this.isInitialPlay = false;
    },
    onPause() {
      this.isPlaying = false;
    },
    onTimeUpdate(event) {
      const currentTime = event.target.currentTime;
      this.progress = currentTime;
      this.duration = event.target.duration || 0;
      this.updateCurrentLyricIndex(currentTime);
    },
    onEnded() {
      this.isPlaying = false;
      this.progress = 0;
      this.currentLyricIndex = 0;
      this.scrollToCurrentLyric();
    },
    handleProgressClick(e) {
      const progressBar = this.$refs.progressBar;
      const rect = progressBar.getBoundingClientRect();
      const pos = (e.clientX - rect.left) / rect.width;
      const audio = this.$refs.audio;
      audio.currentTime = pos * this.duration;
    },
    adjustVolume() {
      const audio = this.$refs.audio;
      audio.volume = this.volume;
    },
    toggleMute() {
      if (this.volume > 0) {
        this.previousVolume = this.volume;
        this.volume = 0;
      } else {
        this.volume = this.previousVolume;
      }
      this.adjustVolume();
    },
    async loadLyrics(musicName) {
      try {
        const lyricsUrl = `/file/lyrics/${musicName}`;
        const response = await request.get(lyricsUrl);
        if (typeof response === 'string') {
          this.lyrics = this.parseLyrics(response);
        } else if (response && response.data) {
          this.lyrics = this.parseLyrics(response.data);
        }
      } catch (error) {
        console.error("Lyrics load failed:", error);
        this.lyrics = [{time: 0, text: "纯音乐，请欣赏"}];
      }
    },
    parseLyrics(rawLyrics) {
      if (!rawLyrics) return [];
      const lines = rawLyrics.split("\n");
      const regex = /\[(\d+):(\d+)\.(\d+)\](.+)/;
      return lines
          .map((line) => {
            const match = line.match(regex);
            if (match) {
              const minutes = parseInt(match[1], 10);
              const seconds = parseInt(match[2], 10);
              const milliseconds = parseInt(match[3], 10);
              const text = match[4].trim();
              return {
                time: minutes * 60 + seconds + milliseconds / 1000,
                text,
              };
            }
            return null;
          })
          .filter((line) => line !== null);
    },
    updateCurrentLyricIndex(currentTime) {
      const index = this.lyrics.findIndex(lyric => currentTime < lyric.time);
      const targetIndex = index === -1 ? this.lyrics.length - 1 : index - 1;

      if (this.currentLyricIndex !== targetIndex) {
        this.currentLyricIndex = targetIndex;
        this.scrollToCurrentLyric();
      }
    },
    scrollToCurrentLyric() {
      if (this.currentLyricIndex < 0) return;
      
      this.$nextTick(() => {
        const container = this.$refs.lyricContainer;
        const activeLine = this.$refs.lyricLines?.[this.currentLyricIndex];
        
        if (container && activeLine) {
          activeLine.scrollIntoView({
            behavior: 'smooth',
            block: 'center'
          });
        }
      });
    },
    seekToLyric(time) {
      const audio = this.$refs.audio;
      audio.currentTime = time;
      this.progress = time;
    },
    togglePlayMode() {
      this.playMode = this.playMode === 'sequential' ? 'loop' : 'sequential';
    },
    formatTime(seconds) {
      if (!seconds) return '0:00';
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins}:${secs.toString().padStart(2, '0')}`;
    },
    toggleSettings() {
      this.showSettings = !this.showSettings;
    },
    updateStyles() {
      document.documentElement.style.setProperty('--lyric-color', this.lyricColor);
      document.documentElement.style.setProperty('--active-lyric-color', this.activeLyricColor);
      document.documentElement.style.setProperty('--lyric-font-size', `${this.lyricFontSize}px`);
      document.documentElement.style.setProperty('--lyric-align', this.lyricAlign);
    },
    setTheme(theme) {
      this.currentTheme = theme.id;
      document.documentElement.style.setProperty('--theme-gradient', theme.gradient);
      document.documentElement.style.setProperty('--accent-gradient', theme.accent);
    },
    changeAlign(align) {
      this.lyricAlign = align;
      this.updateStyles();
      this.saveSettings();
    },
    saveSettings() {
      const settings = {
        lyricColor: this.lyricColor,
        activeLyricColor: this.activeLyricColor,
        lyricFontSize: this.lyricFontSize,
        lyricAlign: this.lyricAlign,
        currentTheme: this.currentTheme
      };
      localStorage.setItem('musicPlayerSettings', JSON.stringify(settings));
    },
    loadSettings() {
      const settings = localStorage.getItem('musicPlayerSettings');
      if (settings) {
        try {
          const parsed = JSON.parse(settings);
          this.lyricColor = parsed.lyricColor || this.lyricColor;
          this.activeLyricColor = parsed.activeLyricColor || this.activeLyricColor;
          this.lyricFontSize = parsed.lyricFontSize || this.lyricFontSize;
          this.lyricAlign = parsed.lyricAlign || this.lyricAlign;
          this.currentTheme = parsed.currentTheme || this.currentTheme;

          const theme = this.themeColors.find(t => t.id === this.currentTheme);
          if (theme) this.setTheme(theme);
          this.updateStyles();
        } catch (e) {
          console.error("Settings parse error");
        }
      } else {
        this.setTheme(this.themeColors[0]);
        this.updateStyles();
      }
    }
  },
  mounted() {
    this.loadSettings();
    window.addEventListener('keydown', (e) => {
      if (e.target.tagName === 'INPUT') return;
      switch(e.code) {
        case 'Space':
          e.preventDefault();
          this.togglePlay();
          break;
        case 'ArrowUp':
          e.preventDefault();
          this.volume = Math.min(1, this.volume + 0.1);
          this.adjustVolume();
          break;
        case 'ArrowDown':
          e.preventDefault();
          this.volume = Math.max(0, this.volume - 0.1);
          this.adjustVolume();
          break;
        case 'ArrowLeft':
          this.$refs.audio.currentTime = Math.max(0, this.progress - 5);
          break;
        case 'ArrowRight':
          this.$refs.audio.currentTime = Math.min(this.duration, this.progress + 5);
          break;
      }
    });
  },
  watch: {
    lyricColor() { this.saveSettings(); },
    activeLyricColor() { this.saveSettings(); },
    lyricFontSize() { this.saveSettings(); },
    currentTheme() { this.saveSettings(); }
  }
};
</script>

<style scoped>
:root {
  --theme-gradient: linear-gradient(135deg, #1e2024 0%, #232526 100%);
  --accent-gradient: linear-gradient(90deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%);
  --lyric-color: #e0e0e0;
  --active-lyric-color: #ffffff;
  --lyric-font-size: 18px;
  --lyric-align: center;
}


.music-player-wrapper {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  color: white;
  background: #111;
}


.dynamic-bg {
  position: absolute;
  top: -10%;
  left: -10%;
  width: 120%;
  height: 120%;
  background-size: cover;
  background-position: center;
  filter: blur(60px) brightness(0.6);
  z-index: 1;
  transition: background-image 0.8s ease;
}

.bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  z-index: 2;
}

.glass-card {
  position: relative;
  z-index: 10;
  width: 900px;
  max-width: 95%;
  height: 600px;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.player-content {
  flex: 1;
  display: flex;
  padding: 40px;
  overflow: hidden;
  gap: 40px;
}

.left-section {
  flex: 0 0 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.right-section {
  flex: 1;
  position: relative;
}

.cover-wrapper {
  position: relative;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.cover-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  animation: rotate 20s linear infinite;
  animation-play-state: paused;
  border: 8px solid rgba(0, 0, 0, 0.8);
  box-sizing: border-box;
}

.cover-image.rotate {
  animation-play-state: running;
}

.disc-hole {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background: #1e1e1e;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.1);
  z-index: 2;
}

.cover-glow {
  position: absolute;
  top: 10%;
  left: 10%;
  width: 80%;
  height: 80%;
  border-radius: 50%;
  background: var(--accent-gradient);
  filter: blur(40px);
  opacity: 0;
  transition: opacity 1s;
  z-index: -1;
}

.cover-glow.playing {
  opacity: 0.6;
}

.track-info {
  margin-top: 30px;
  text-align: center;
}

.title {
  font-size: 24px;
  margin: 0 0 8px;
  font-weight: 700;
  letter-spacing: 0.5px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.artist {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.lyrics-mask {
  width: 100%;
  height: 100%;
  mask-image: linear-gradient(to bottom,
  transparent 0%,
  black 15%,
  black 85%,
  transparent 100%
  );
  -webkit-mask-image: linear-gradient(to bottom,
  transparent 0%,
  black 15%,
  black 85%,
  transparent 100%
  );
}

.lyrics-scroll {
  height: 100%;
  overflow-y: auto;
  scrollbar-width: none;
  display: flex;
  flex-direction: column;
  padding: 0 30px;
  box-sizing: border-box;
  width: 100%;
}

.lyrics-scroll::-webkit-scrollbar {
  display: none;
}

.padding-space {
  min-height: 50%;
}

.lyric-line {
  min-height: 40px;
  line-height: 1.6;
  margin: 12px 0;
  color: var(--lyric-color);
  font-size: var(--lyric-font-size);
  text-align: var(--lyric-align);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  opacity: 0.5;
  filter: blur(0.5px);

  white-space: pre-wrap;
  word-break: break-word;
  max-width: 100%;
}

.lyric-line:hover {
  opacity: 0.8;
}

.lyric-line.near {
  opacity: 0.8;
  filter: blur(0);
}

.lyric-line.active {
  color: var(--active-lyric-color);
  font-size: calc(var(--lyric-font-size) * 1.2);
  font-weight: bold;
  opacity: 1;
  filter: blur(0);
  transform: scale(1.05);
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.4);
  margin: 20px 0; 
}

.no-lyrics {
  text-align: center;
  margin-top: 50%;
  color: rgba(255, 255, 255, 0.4);
}


.control-bar {
  background: rgba(0, 0, 0, 0.2);
  padding: 15px 40px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.progress-area {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.progress-track {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  cursor: pointer;
  position: relative;
  transition: height 0.2s;
}

.progress-track:hover {
  height: 6px;
}

.progress-fill {
  height: 100%;
  background: var(--accent-gradient);
  border-radius: 2px;
  position: relative;
}

.progress-thumb {
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%) scale(0);
  width: 12px;
  height: 12px;
  background: #fff;
  border-radius: 50%;
  transition: transform 0.2s;
}

.progress-track:hover .progress-thumb {
  transform: translateY(-50%) scale(1);
}

.controls-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.icon-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;
  cursor: pointer;
  padding: 10px;
  transition: color 0.3s;
}

.icon-btn:hover {
  color: #fff;
}

.play-fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: #fff;
  color: #000;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(255, 255, 255, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
  margin: 0 20px;
}

.play-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(255, 255, 255, 0.5);
}

.buttons-center {
  display: flex;
  align-items: center;
}

.volume-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 120px;
}

.volume-slider-container {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  display: flex;
  align-items: center;
}

.volume-range {
  -webkit-appearance: none;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(#fff, #fff);
  background-repeat: no-repeat;
  background-color: transparent;
  border-radius: 2px;
  cursor: pointer;
}

.volume-range::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 12px;
  width: 12px;
  border-radius: 50%;
  background: #fff;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.volume-wrapper:hover .volume-range::-webkit-slider-thumb {
  opacity: 1;
}

.settings-trigger {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 100;
  color: rgba(255, 255, 255, 0.6);
  font-size: 20px;
  cursor: pointer;
  transition: transform 0.5s;
}

.settings-trigger:hover, .settings-trigger.active {
  color: #fff;
  transform: rotate(90deg);
}

.settings-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 300px;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(30px);
  z-index: 99;
  padding: 20px;
  box-sizing: border-box;
  box-shadow: -5px 0 20px rgba(0, 0, 0, 0.5);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 15px;
}

.close-btn {
  font-size: 24px;
  cursor: pointer;
}

.setting-group {
  margin-bottom: 25px;
}

.setting-group label {
  display: block;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 10px;
}

.theme-grid {
  display: flex;
  gap: 15px;
}

.theme-dot {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: transform 0.2s;
}

.theme-dot.active {
  border-color: #fff;
  transform: scale(1.1);
}

.color-pickers {
  display: flex;
  gap: 20px;
}

.color-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 12px;
}

.color-item input[type="color"] {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 8px;
  background: none;
  cursor: pointer;
}

.range-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.styled-range {
  width: 100%;
  margin-top: 10px;
  accent-color: #fff;
}

.align-options {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  padding: 4px;
}

.align-options button {
  flex: 1;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
}

.align-options button.active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from, .fade-slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

@media (max-width: 768px) {
  .glass-card {
    height: 100vh;
    width: 100%;
    border-radius: 0;
    max-height: none;
  }

  .player-content {
    flex-direction: column;
    padding: 20px;
    gap: 20px;
  }

  .left-section {
    flex: 0 0 auto;
  }

  .cover-wrapper {
    width: 200px;
    height: 200px;
  }

  .title {
    font-size: 20px;
  }

  .settings-panel {
    width: 100%;
  }
}
</style>