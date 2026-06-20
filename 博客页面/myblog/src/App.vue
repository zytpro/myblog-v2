<script setup>
import {ref, onMounted, onUnmounted, computed} from 'vue'
import request from './utills/request.js'
import {useRouter} from 'vue-router'
import {useAuthStore} from './stores/auth.js'
import {startTokenAutoRefresh, stopTokenAutoRefresh} from './utills/auth.js'
import userActivityManager from './utills/userActivityManager.js'

const router = useRouter()
const drawer = ref(false)
const authStore = useAuthStore()
const isScrolled = ref(false)
const homeSettings = ref({
  footerQuote: '云想衣裳花想容,春风拂槛露华浓。',
  icpLicense: '滇ICP备2025053841号-1',
  contactEmail: '1363269065@qq.com'
})

const fetchHomeSettings = async () => {
  try {
    const response = await request.get('/home-settings/get')
    if (response.code === 0 && response.data) {
      homeSettings.value = {
        ...homeSettings.value,
        ...response.data
      }
    }
  } catch (error) {
    console.error('获取首页设置失败:', error)
  }
}

const staticNav = [
  {title: '首页', path: '/', icon: 'mdi-home'}
]

const adminNav = computed(() => ({
  title: '后台管理',
  path: authStore.loggedIn ? '/admin' : '/admin/login',
  icon: '/NavbarIco/setting.svg'
}))

const iconMap = {
  '热搜': '/NavbarIco/HotSearch.svg',
  '我的小家': '/NavbarIco/MyHome.svg',
  'AI聊天室': '/NavbarIco/ChatRoom.svg',
  '留言墙': '/NavbarIco/liuyanqiang.svg'
}

const pathMap = {
  '热搜': '/hotsearch',
  '我的小家': '/myhome',
  'AI聊天室': '/chatroom',
  '留言墙': '/message',
  '留言板': '/message'
}

const apiNav = ref([])
const fetchNavbar = async () => {
  try {
    const res = await request.get('/navbar/list')
    let list = []
    if (Array.isArray(res)) list = res
    else if (Array.isArray(res?.data)) list = res.data
    apiNav.value = list
        .filter(i => i.state === '1' || i.state === 1)
        .map(i => {
          let finalPath = i.path || i.url
          if (!finalPath && pathMap[i.name]) {
            finalPath = pathMap[i.name]
          }
          if (!finalPath) {
            finalPath = '#'
          }
          return {
            title: i.name,
            path: finalPath,
            icon: i.icon || iconMap[i.name] || 'mdi-dots-horizontal'
          }
        })
  } catch (e) {
    apiNav.value = []
  }
}

const navItemsAll = computed(() => [staticNav[0], ...apiNav.value, adminNav.value])

const isSvgIcon = (icon) => typeof icon === 'string' && (icon.endsWith('.svg') || icon.startsWith('/'))

const originalTitle = '我的博客'
const handleFocus = () => {
  document.title = '欢迎回来~o(*￣▽￣*)ブ'
}
const handleBlur = () => {
  document.title = '不要走！/(ㄒoㄒ)/~~'
}

const handleScroll = () => {
  isScrolled.value = window.scrollY > 60
}

const handleMobileNavClick = (item) => {
  drawer.value = false;
  if (item.title === '后台管理') {
    const url = authStore.loggedIn ? '/admin' : '/admin/login';
    window.open(url, '_blank', 'noopener,noreferrer');
  } else {
    router.push(item.path);
  }
}

onMounted(async () => {
  window.addEventListener('focus', handleFocus)
  window.addEventListener('blur', handleBlur)
  window.addEventListener('scroll', handleScroll)
  document.title = '欢迎回来~o(*￣▽￣*)ブ'

  // 异步验证登录状态
  await authStore.validateLoginStatus()
  
  // 使用异步验证来更新登录状态
  startTokenAutoRefresh(async () => {
    await authStore.validateLoginStatus()
  })
  
  userActivityManager.startListening(async () => {
    await authStore.validateLoginStatus()
  })
  
  fetchNavbar()
  fetchHomeSettings()
})

onUnmounted(() => {
  window.removeEventListener('focus', handleFocus)
  window.removeEventListener('blur', handleBlur)
  window.removeEventListener('scroll', handleScroll)
  document.title = originalTitle
  stopTokenAutoRefresh()
  userActivityManager.stopListening()
})
</script>

<template>
  <v-app class="modern-app">
    <v-app-bar
        elevation="0"
        class="jiujiu-navbar"
        :class="{ 'nav-scrolled': isScrolled }"
        v-if="$route.meta.hideLayout !== true"
    >
      <v-container class="d-flex align-center navbar-container">
        <router-link to="/" class="modern-logo">
          <div class="logo-container">
            <div class="logo-icon">
              <v-icon size="28" class="logo-icon-svg">mdi-hexagon-multiple</v-icon>
            </div>
            <span class="logo-text">我的博客</span>
          </div>
        </router-link>

        <div class="d-none d-md-flex modern-nav-menu">
          <div
              v-for="item in navItemsAll"
              :key="item.title + item.path"
              class="nav-item-wrapper"
          >
            <template v-if="item.title === '后台管理'">
              <a
                  :href="authStore.loggedIn ? '/admin' : '/admin/login'"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="modern-nav-link"
              >
                <div class="nav-icon-container">
                  <template v-if="isSvgIcon(item.icon)">
                    <img :src="item.icon" class="nav-img-icon" alt=""/>
                  </template>
                  <template v-else>
                    <v-icon size="20" class="nav-icon">{{ item.icon }}</v-icon>
                  </template>
                </div>
                <span class="nav-text">{{ item.title }}</span>
              </a>
            </template>
            <template v-else>
              <router-link :to="item.path" class="modern-nav-link">
                <div class="nav-icon-container">
                  <template v-if="isSvgIcon(item.icon)">
                    <img :src="item.icon" class="nav-img-icon" alt=""/>
                  </template>
                  <template v-else>
                    <v-icon size="20" class="nav-icon">{{ item.icon }}</v-icon>
                  </template>
                </div>
                <span class="nav-text">{{ item.title }}</span>
              </router-link>
            </template>
          </div>
        </div>

        <v-spacer></v-spacer>
        <div class="d-none d-md-flex navbar-decorations">
          <div class="cat-animation">
            <img src="/pixel-cat.gif" alt="猫猫动画" class="cat-gif"/>
          </div>
        </div>
        <v-btn
            @click="drawer = !drawer"
            class="d-md-none mobile-menu-btn"
            icon
            variant="text"
            :color="isScrolled ? '#333' : 'white'"
        >
          <v-icon size="24">mdi-menu</v-icon>
        </v-btn>
      </v-container>
    </v-app-bar>

    <v-navigation-drawer
        v-model="drawer"
        location="right"
        temporary
        class="modern-mobile-drawer"
        v-if="$route.meta.hideLayout !== true"
    >
      <div class="mobile-drawer-header">
        <div class="mobile-logo">
          <v-icon size="24" color="white">mdi-hexagon-multiple</v-icon>
          <span>我的博客</span>
        </div>
        <v-btn @click="drawer = false" icon variant="text" color="white" size="small">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>
      <v-list class="mobile-nav-list">
        <v-list-item
            v-for="item in navItemsAll"
            :key="item.title + item.path"
            class="modern-mobile-nav-item"
            @click="handleMobileNavClick(item)"
        >
          <template #prepend>
            <div class="mobile-nav-icon">
              <img v-if="isSvgIcon(item.icon)" :src="item.icon" class="nav-img-icon" alt=""/>
              <v-icon v-else :icon="item.icon" size="20"></v-icon>
            </div>
          </template>
          <template #title>
            <span class="mobile-nav-text">{{ item.title }}</span>
          </template>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main class="modern-main" style="padding-top: 0 !important;">
      <router-view/>
    </v-main>
    <v-footer class="jiujiu-footer" padless v-if="$route.meta.hideLayout !== true">
      <div class="footer-waves-container">
        <div id="footerWave1"></div>
        <div id="footerWave2"></div>
      </div>
      <div class="footer-content">
        <div class="footer-text">
          <p>{{ homeSettings.footerQuote }}</p>
          <p>{{ homeSettings.icpLicense }}</p>
          <p>联系邮箱：{{ homeSettings.contactEmail }}</p>
        </div>
        <div class="footer-icons">
          <div class="footer-icon">🚀</div>
          <div class="footer-icon">🏠</div>
          <div class="footer-icon">⚙️</div>
        </div>
      </div>
    </v-footer>
  </v-app>
</template>

<style scoped>
@import url('https://fonts.loli.net/css2?family=Fredoka:wght@400;600&family=Noto+Sans+SC:wght@400;700&family=ZCOOL+KuaiLe&display=swap');

.modern-app {
  background: transparent !important;
  min-height: 100vh;
}

.jiujiu-navbar {
  position: fixed !important;
  top: 0;
  left: 0;
  width: 100%;
  height: 60px !important;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0) 100%) !important;
  backdrop-filter: none;
  box-shadow: none !important;
}

.jiujiu-navbar :deep(.v-toolbar__content),
.jiujiu-navbar :deep(.v-toolbar__extension) {
  background: transparent !important;
  height: 60px !important;
}

.logo-text {
  font-size: 1.4rem;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
  transition: all 0.3s ease;
}

.logo-icon-svg {
  color: #fff;
  transition: color 0.3s;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.6));
}

.modern-nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  text-decoration: none;
  position: relative;
  transition: all 0.3s;
  color: #fff !important;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.9);
  font-weight: 600;
}

.modern-nav-link::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #fff;
  border-radius: 2px;
  transform: scaleX(0);
  transform-origin: center;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.6);
}

.modern-nav-link:hover::after {
  transform: scaleX(1);
}

.modern-nav-link:hover {
  transform: translateY(-2px);
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
}

.nav-img-icon {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.8));
  width: 18px;
  height: 18px;
  display: inline-block;
  transition: all 0.3s;
}

.nav-icon {
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
  transition: transform 0.3s;
}

.jiujiu-navbar.nav-scrolled {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05) !important;
}

.jiujiu-navbar.nav-scrolled .logo-text {
  color: #333;
  text-shadow: none;
}

.jiujiu-navbar.nav-scrolled .logo-icon-svg {
  color: #667eea;
  filter: none;
}

.jiujiu-navbar.nav-scrolled .modern-nav-link {
  color: #4c4948 !important;
  text-shadow: none;
}

.jiujiu-navbar.nav-scrolled .nav-img-icon {
  filter: none;
}

.jiujiu-navbar.nav-scrolled .nav-icon {
  text-shadow: none;
}

.jiujiu-navbar.nav-scrolled .modern-nav-link::after {
  background: linear-gradient(to right, #667eea, #764ba2);
  box-shadow: none;
}

.jiujiu-navbar.nav-scrolled .modern-nav-link:hover {
  color: #764ba2 !important;
  background: rgba(118, 75, 162, 0.08);
}

.navbar-container {
  padding: 0 24px;
  max-width: 1400px !important;
}

.modern-logo {
  text-decoration: none;
  transition: all 0.3s ease;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.jiujiu-navbar.nav-scrolled .logo-icon {
  background: transparent;
  box-shadow: none;
}

.logo-container:hover .logo-icon {
  transform: rotate(15deg);
}

.modern-nav-menu {
  display: flex;
  gap: 15px;
  margin-left: 40px;
}

.modern-nav-link:hover .nav-icon {
  transform: scale(1.1);
}

.nav-text {
  font-weight: 600;
  font-size: 15px;
}

.navbar-decorations {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cat-gif {
  width: 40px;
  height: 40px;
  border-radius: 8px;
}

.mobile-menu-btn {
  transition: all 0.3s;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.modern-mobile-drawer {
  background: rgba(30, 30, 40, 0.95) !important;
  backdrop-filter: blur(20px) !important;
  border-left: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.mobile-drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
}

.mobile-nav-list {
  padding: 20px 0;
}

.modern-mobile-nav-item {
  margin: 8px 16px;
  border-radius: 12px;
  background: transparent;
  color: white !important;
}

.modern-mobile-nav-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

.mobile-nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
}

.mobile-nav-text {
  font-weight: 500;
  color: white;
}

.jiujiu-footer {
  background: linear-gradient(135deg, rgba(44, 62, 80, 0.9) 0%, rgba(52, 73, 94, 0.9) 100%),
  url('/back.webp') center/cover no-repeat;
  position: relative;
  overflow: hidden;
  padding: 0 !important;
  min-height: 200px;
  margin-top: 50px;
}

.footer-waves-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100px;
  overflow: hidden;
  z-index: 1;
  pointer-events: none;
}

#footerWave1 {
  height: 84px;
  background: url('/img/bannerWave1.png') repeat-x;
  position: absolute;
  top: 0;
  width: 200%;
  left: 0;
  z-index: 10;
  animation: gradientBG 120s linear infinite;
  transform: rotate(180deg);
}

#footerWave2 {
  height: 100px;
  background: url('/img/bannerWave2.png') repeat-x;
  position: absolute;
  top: 0;
  width: 400%;
  left: 0;
  z-index: 15;
  animation: gradientBG 30s linear infinite;
  transform: rotate(180deg);
}

@keyframes gradientBG {
  0% {
    transform: translateX(0) rotate(180deg);
  }
  100% {
    transform: translateX(-50%) rotate(180deg);
  }
}

.footer-content {
  position: relative;
  z-index: 2;
  padding: 80px 20px 30px;
  text-align: center;
  max-width: 1200px;
  margin: 0 auto;
}

.footer-text {
  color: white;
  margin-bottom: 20px;
}

.footer-text p {
  margin: 6px 0;
  font-size: 13px;
  line-height: 1.5;
  opacity: 0.9;
}

.footer-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.footer-icon {
  width: 35px;
  height: 35px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.footer-icon:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

@media (max-width: 768px) {
  .logo-text {
    font-size: 1.1rem;
  }

  #footerWave1 {
    height: 60px;
  }

  #footerWave2 {
    height: 70px;
  }

  .footer-content {
    padding-top: 60px;
  }
}
</style>