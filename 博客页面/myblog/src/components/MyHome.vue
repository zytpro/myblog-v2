<template>
  <div class="page-wrapper">

    <!-- 顶部区域 -->
    <div class="hero-section">
      <div class="hero-bg-img">
        <img src="/img/back.png" alt="Background">
        <div class="hero-mask"></div>
      </div>

      <div class="hero-content">
        <div class="glass-container">
          <div class="hero-avatar-box">
            <div class="avatar-circle">
              <img :src="boyAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="Boy">
            </div>
            <span class="hero-name">{{ boyName || '先生' }}</span>
          </div>
          <div class="hero-heart-box">
            <div class="beating-heart">❤</div>
            <div class="love-days">Love</div>
          </div>
          <div class="hero-avatar-box">
            <div class="avatar-circle">
              <img :src="girlAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
                   alt="Girl">
            </div>
            <span class="hero-name">{{ girlName || '小姐' }}</span>
          </div>
        </div>
      </div>

      <div class="waves-area">
        <svg class="waves-svg" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
             viewBox="0 24 150 28" preserveAspectRatio="none" shape-rendering="auto">
          <defs>
            <path id="gentle-wave" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z"/>
          </defs>
          <g class="parallax">
            <use xlink:href="#gentle-wave" x="48" y="0" fill="rgba(247, 249, 254, 0.3)"/>
            <use xlink:href="#gentle-wave" x="48" y="3" fill="rgba(247, 249, 254, 0.5)"/>
            <use xlink:href="#gentle-wave" x="48" y="5" fill="rgba(247, 249, 254, 0.7)"/>
            <use xlink:href="#gentle-wave" x="48" y="7" fill="#f7f9fe"/>
          </g>
        </svg>
      </div>
    </div>

    <!-- 核心内容区 -->
    <div class="love-page">
      <div class="love-container">

        <!-- 打字机文字 -->
        <div class="intro-section">
          <h1 class="main-title">Our Love Story</h1>
          <div class="typewriter-box">
            <span class="subtitle">{{ typedText }}</span>
            <span class="cursor">|</span>
          </div>
        </div>

        <!-- 计时器区域 -->
        <div class="timer-section">
          <div class="timer-paper">
            <div class="timer-title">这是我们一起走过的</div>
            <div class="timer-digits">
              <span class="prefix">第</span>
              <div class="digit-pair"><span class="num">{{ years }}</span><span class="unit">年</span></div>
              <div class="digit-pair"><span class="num">{{ months }}</span><span class="unit">月</span></div>
              <div class="digit-pair"><span class="num">{{ days }}</span><span class="unit">日</span></div>
              <div class="digit-pair"><span class="num">{{ hours }}</span><span class="unit">时</span></div>
              <div class="digit-pair"><span class="num">{{ minutes }}</span><span class="unit">分</span></div>
              <div class="digit-pair"><span class="num">{{ seconds }}</span><span class="unit">秒</span></div>
            </div>
            <div class="timer-footer">
              我们已经相爱了 {{ days + (years * 365) + (months * 30) }} 天
            </div>
          </div>
        </div>

        <!-- 导航条 -->
        <div class="nav-bar">
          <div class="nav-item" :class="{ active: currentView === 'timeline' }" @click="navigateTo('timeline')">
            <span>📅 点点滴滴</span>
          </div>
          <div class="nav-item" :class="{ active: currentView === 'photos' }" @click="navigateTo('photos')">
            <span>📸 时光相册</span>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="content-wrapper">
          <transition name="fade-slide" mode="out-in">
            <!-- 时间轴 -->
            <div v-if="currentView === 'timeline'" class="timeline-container">
              <div class="line-center"></div>
              <div v-if="timelineMessages.length === 0" class="empty-tip">这里空空如也，快去记录吧~</div>
              <div v-for="(msg, index) in timelineMessages" :key="index"
                   class="timeline-row" :class="msg.position" v-animate-on-scroll>
                <div class="timeline-dot"></div>
                <div class="paper-card">
                  <div class="card-header">
                    <img :src="msg.avatar" class="tiny-avatar">
                    <span class="time-tag">{{ formatDate(msg.time) }}</span>
                  </div>
                  <div class="card-body">{{ msg.text }}</div>
                  <div class="corner-fold"></div>
                </div>
              </div>
            </div>

            <!-- 照片墙 -->
            <div v-else-if="currentView === 'photos'" class="photo-container">
              <div v-if="isLoading" class="loading-box">
                <div class="spinner"></div>
                <p>正在冲洗照片...</p></div>
              <div v-else class="polaroid-grid">
                <div v-for="(photo, index) in photoItems" :key="index"
                     class="polaroid-item" @click="previewImage(photo)">
                  <div class="photo-frame"><img :src="photo.image" loading="lazy"></div>
                  <div class="photo-info"><p>{{ photo.title }}</p><span>{{ photo.date }}</span></div>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <!-- 图片预览 -->
        <transition name="zoom">
          <div v-if="showPreview" class="lightbox" @click="closePreview">
            <div class="lightbox-content" @click.stop>
              <img :src="previewItem.image">
              <div class="lightbox-caption"><h3>{{ previewItem.title }}</h3>
                <p>{{ previewItem.date }}</p></div>
              <button class="close-btn" @click="closePreview">×</button>
            </div>
          </div>
        </transition>

        <!-- 底部 -->
        <div class="footer-info">
          <p>Forever Love · {{ boyName }} & {{ girlName }}</p>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import request from '../utills/request';

export default {
  name: "LovePage",
  directives: {
    animateOnScroll: {
      mounted(el) {
        const observer = new IntersectionObserver((entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              el.classList.add('animate-in');
              observer.unobserve(el);
            }
          });
        }, {threshold: 0.1});
        observer.observe(el);
      }
    }
  },
  data() {
    return {
      currentView: 'timeline',
      boyName: '', girlName: '',
      boyAvatar: '', girlAvatar: '',
      startDate: new Date(),

      years: 0, months: 0, days: 0, hours: 0, minutes: 0, seconds: 0,
      timer: null,

      timelineMessages: [],
      photoItems: [],
      isLoading: false,
      showPreview: false,
      previewItem: null,

      fullSubtitle: "斯人若彩虹，遇上方知有",
      typedText: "",
      typeIndex: 0
    };
  },
  async mounted() {
    await this.fetchLoveInfo();
    this.startTypewriter();
    this.fetchTimelineData();
    this.startTimer();
  },
  beforeUnmount() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {
    startTypewriter() {
      if (this.typeIndex < this.fullSubtitle.length) {
        this.typedText += this.fullSubtitle.charAt(this.typeIndex);
        this.typeIndex++;
        setTimeout(this.startTypewriter, 200);
      }
    },
    async fetchLoveInfo() {
      try {
        const res = await request('/love-room');
        if (res.code === 0) {
          const {boyName, girlName, boyAvatar, girlAvatar, time} = res.data;
          this.boyName = boyName;
          this.girlName = girlName;
          this.boyAvatar = boyAvatar;
          this.girlAvatar = girlAvatar;
          this.startDate = new Date(time);
        }
      } catch (e) {
        console.error(e);
      }
    },
    async navigateTo(view) {
      if (this.currentView === view) return;
      this.currentView = view;
      if (view === 'timeline' && !this.timelineMessages.length) await this.fetchTimelineData();
      if (view === 'photos' && !this.photoItems.length) await this.fetchPhotoData();
    },
    async fetchTimelineData() {
      try {
        const res = await request('/love-time');
        if (res.code === 0) {
          this.timelineMessages = res.data.map(item => ({
            position: item.creatPeople === '1' ? 'left' : 'right',
            avatar: item.creatPeople === '1' ? this.boyAvatar : this.girlAvatar,
            text: item.content,
            time: item.createTime || item.time
          }));
        }
      } catch (e) {
        console.error(e);
      }
    },
    async fetchPhotoData() {
      this.isLoading = true;
      try {
        const res = await request('/life-time');
        if (res.code === 0) {
          this.photoItems = res.data.map(item => ({
            title: item.content,
            date: item.createTime,
            image: item.url
          }));
        }
      } catch (e) {
        console.error(e);
      } finally {
        this.isLoading = false;
      }
    },
    formatDate(d) {
      return d ? d.split(' ')[0] : '';
    },
    startTimer() {
      this.updateTime();
      this.timer = setInterval(this.updateTime, 1000);
    },
    updateTime() {
      const current = new Date();
      const diff = current - this.startDate;
      this.years = Math.floor(diff / (1000 * 60 * 60 * 24 * 365));
      this.months = Math.floor((diff % (1000 * 60 * 60 * 24 * 365)) / (1000 * 60 * 60 * 24 * 30));
      this.days = Math.floor((diff % (1000 * 60 * 60 * 24 * 30)) / (1000 * 60 * 60 * 24));
      this.hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
      this.minutes = current.getMinutes();
      this.seconds = current.getSeconds();
    },
    previewImage(item) {
      this.previewItem = item;
      this.showPreview = true;
      document.body.style.overflow = 'hidden';
    },
    closePreview() {
      this.showPreview = false;
      document.body.style.overflow = '';
    }
  }
};
</script>

<style scoped>
:root {
  --blue-theme: #4facfe;
  --text-main: #555;
}

.page-wrapper {
  min-height: 100vh;
  width: 100%;
  color: var(--text-main);
}


.hero-section {
  position: relative;
  width: 100%;
  height: 60vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-bg-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
}

.hero-bg-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  animation: hero-breath 18s ease-in-out infinite;
  transform-origin: center;
}

.hero-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
}

.hero-content {
  position: relative;
  z-index: 10;
  margin-top: -40px;
}

.glass-container {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 30px 60px;
  display: flex;
  align-items: center;
  gap: 40px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.hero-avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid rgba(255, 255, 255, 0.8);
  overflow: hidden;
  transition: transform 0.5s;
  background: #fff;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-avatar-box:hover .avatar-circle {
  transform: rotate(10deg) scale(1.1);
}

.hero-name {
  margin-top: 10px;
  color: #fff;
  font-weight: bold;
  font-size: 1.2rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-heart-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.beating-heart {
  font-size: 3rem;
  color: #ff6b81;
  animation: heartbeat 1.2s infinite;
  text-shadow: 0 0 20px rgba(255, 107, 129, 0.6);
}

.love-days {
  color: #fff;
  font-size: 0.9rem;
  margin-top: 5px;
  opacity: 0.9;
}

.waves-area {
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 80px;
  z-index: 5;
  margin-bottom: -1px;
}

.waves-svg {
  width: 100%;
  height: 100%;
}

.parallax > use {
  animation: move-forever 25s cubic-bezier(.55, .5, .45, .5) infinite;
}

.parallax > use:nth-child(1) {
  animation-delay: -2s;
  animation-duration: 7s;
}

.parallax > use:nth-child(2) {
  animation-delay: -3s;
  animation-duration: 10s;
}

.parallax > use:nth-child(3) {
  animation-delay: -4s;
  animation-duration: 13s;
}

.parallax > use:nth-child(4) {
  animation-delay: -5s;
  animation-duration: 20s;
}

@keyframes move-forever {
  0% {
    transform: translate3d(-90px, 0, 0);
  }
  100% {
    transform: translate3d(85px, 0, 0);
  }
}

@keyframes hero-breath {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.04);
  }
  100% {
    transform: scale(1);
  }
}

.love-page {
  background-color: #ffffff;
  background-image: linear-gradient(90deg, rgba(0, 0, 0, 1) 1px, transparent 1px);
  background-size: 30px 100%;
  padding-bottom: 50px;
  position: relative;
  z-index: 10;
}

.love-container {
  max-width: 1300px;
  margin: 0 auto;
  padding: 0 20px;
}

.intro-section {
  text-align: center;
  padding: 40px 0 20px;
}

.main-title {
  font-size: 3rem;
  margin-bottom: 15px;
  background: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 1.2rem;
  color: #777;
  letter-spacing: 4px;
}

.cursor {
  animation: blink 1s infinite;
}

.timer-section {
  display: flex;
  justify-content: center;
  margin-bottom: 60px;
}

.timer-paper {
  background-color: transparent;
  box-shadow: none;
  border-radius: 0;

  padding: 40px 20px;
  text-align: center;
  width: 100%;
}

.timer-title {
  color: #4facfe;
  font-size: 1.6rem;
  margin-bottom: 20px;
}

.timer-digits {
  display: flex;
  justify-content: center;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 5px;
  line-height: 1;
}

/* 调整字体颜色为纯黑，对比更清晰 */
.prefix {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-right: 5px;
}

.digit-pair {
  display: flex;
  align-items: baseline;
}

.num {
  font-size: 3.5rem;
  font-weight: 600;
  color: #000;
  margin: 0 2px;
}

.unit {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-right: 8px;
}

.timer-footer {
  margin-top: 25px;
  font-size: 1rem;
  color: #666;
  font-weight: bold;
  letter-spacing: 1px;
}

.nav-bar {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 50px;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.1);
  padding-bottom: 20px;
}

.nav-item {
  font-size: 1.2rem;
  color: #888;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.nav-item:hover {
  transform: translateY(-3px);
  color: #4facfe;
}

.nav-item.active {
  color: #4facfe;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.2);
}

.timeline-container {
  position: relative;
  padding: 20px 0;
}

.line-center {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e0e0e0;
  transform: translateX(-50%);
}

.timeline-row {
  display: flex;
  margin-bottom: 50px;
  position: relative;
  opacity: 0;
  transform: translateY(30px);
  transition: 0.6s;
}

.timeline-row.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.timeline-dot {
  position: absolute;
  left: 50%;
  top: 20px;
  width: 16px;
  height: 16px;
  background: #4facfe;
  border: 4px solid #fff;
  border-radius: 50%;
  transform: translateX(-50%);
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.paper-card {
  width: 45%;
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  position: relative;
  border: 1px solid #f0f0f0;
}

.corner-fold {
  position: absolute;
  bottom: 0;
  right: 0;
  border-width: 0 0 20px 20px;
  border-style: solid;
  border-color: transparent transparent #f0f2f5 rgba(0, 0, 0, 0.05);
}

.timeline-row.left {
  justify-content: flex-start;
}

.timeline-row.right {
  justify-content: flex-end;
}

.timeline-row.left .paper-card {
  margin-right: 60px;
  margin-left: auto;
}

.timeline-row.right .paper-card {
  margin-left: 60px;
  margin-right: auto;
}

.card-header {
  display: flex;
  align-items: center;
  border-bottom: 1px dashed #eee;
  padding-bottom: 10px;
  margin-bottom: 15px;
}

.tiny-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 10px;
}

.time-tag {
  color: #aaa;
  font-size: 0.9rem;
}

.card-body {
  color: #444;
  line-height: 1.8;
  font-size: 1rem;
}

.polaroid-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 30px;
}

.polaroid-item {
  background: #fff;
  padding: 12px 12px 40px 12px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.07);
  transition: transform 0.3s;
  cursor: pointer;
}

.polaroid-item:hover {
  transform: translateY(-5px) scale(1.02);
  z-index: 10;
}

.photo-frame img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.photo-info {
  text-align: center;
  margin-top: 15px;
}

.lightbox {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.lightbox-content {
  text-align: center;
}

.lightbox-content img {
  max-height: 80vh;
  max-width: 90vw;
  border: 10px solid #fff;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
}

.close-btn {
  position: absolute;
  top: 30px;
  right: 30px;
  border: none;
  background: none;
  font-size: 3rem;
  cursor: pointer;
  color: #333;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

@keyframes heartbeat {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@media (max-width: 768px) {
  .hero-section {
    height: 50vh;
  }

  .glass-container {
    padding: 20px;
    gap: 20px;
  }

  .avatar-circle {
    width: 70px;
    height: 70px;
  }

  .beating-heart {
    font-size: 2rem;
  }

  .timer-paper {
    padding: 30px 10px;
  }

  .num {
    font-size: 2.2rem;
  }

  .unit {
    font-size: 1rem;
    margin-right: 4px;
  }

  .timer-title {
    font-size: 1.3rem;
  }

  .timeline-row {
    flex-direction: column;
  }

  .timeline-row.left .paper-card, .timeline-row.right .paper-card {
    width: auto;
    margin: 0 0 0 30px !important;
  }

  .line-center {
    left: 15px;
  }

  .timeline-dot {
    left: 15px;
  }
}
</style>