import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, isLoggedInAsync } from '../utills/auth.js'
import Home from '../views/Home.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminHome from '../views/admin/AdminHome.vue'
import Profile from '../components/Profile.vue'
import Dashboard from '../components/Dashboard.vue'
import PictureManage from '../components/PictureManage.vue'
import VideoManage from '../components/VideoManage.vue'
import MucicManage from '../components/MucicManage.vue'
import MusicPlay from '../components/MusicPlay.vue'
import ArticleManage from '../components/ArticleManage.vue'
import CategoryManage from '../components/CategoryManage.vue'
import CommentManage from '../components/CommentManage.vue'
import TagsManage from '../components/TagsManage.vue'
import HotSearchManage from '../components/HotSearchManage.vue'
import AnnouncementManage from '../components/AnnouncementManage.vue'
import CarouseManage from '../components/CarouseManage.vue'
import NavbarManage from '../components/NavbarManage.vue'
import HomeSettingsManage from '../components/HomeSettingsManage.vue'
import LifeTimeManage from '../components/LifeTimeManage.vue'
import LoveRoomManage from '../components/LoveRoomManage.vue'
import LoveTimeManage from '../components/LoveTimeManage.vue'
import ArticleDetailPage from '../views/ArticleDetailPage.vue'
import HotSearch from '../components/HotSearch.vue'
import MyHome from '../components/MyHome.vue'
import MessageBoard from '../components/MessageBoard.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/hotsearch',
    name: 'HotSearch',
    component: HotSearch
  },
  {
    path: '/myhome',
    name: 'MyHome',
    component: MyHome
  },
  {
    path: '/message',
    name: 'MessageBoard',
    component: MessageBoard
  },
  {
    path: '/music-player',
    name: 'MusicPlayer',
    component: MusicPlay,
    meta: { hideLayout: true }
  },

  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetailPage
  },

  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: { hideLayout: true }
  },

  {
    path: '/admin',
    component: AdminHome,
    meta: { hideLayout: true, requiresAuth: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: Dashboard,
        meta: { title: '后台首页', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: Profile,
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'picture',
        name: 'AdminPicture',
        component: PictureManage,
        meta: { title: '图片管理', requiresAuth: true }
      },
      {
        path: 'video',
        name: 'AdminVideo',
        component: VideoManage,
        meta: { title: '视频管理', requiresAuth: true }
      },
      {
        path: 'music',
        name: 'AdminMusic',
        component: MucicManage,
        meta: { title: '音乐管理', requiresAuth: true }
      },
      {
        path: 'article',
        name: 'AdminArticle',
        component: ArticleManage,
        meta: { title: '文章管理', requiresAuth: true }
      },
      {
        path: 'category',
        name: 'AdminCategory',
        component: CategoryManage,
        meta: { title: '分类管理', requiresAuth: true }
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: TagsManage,
        meta: { title: '标签管理', requiresAuth: true }
      },
      {
        path: 'commentManage',
        name: 'AdminCommentManage',
        component: CommentManage,
        meta: { title: '评论管理', requiresAuth: true }
      },
      {
        path: 'hotSearch',
        name: 'AdminHotSearch',
        component: HotSearchManage,
        meta: { title: '热搜api', requiresAuth: true }
      },
      {
        path: 'announcement',
        name: 'AdminAnnouncement',
        component: AnnouncementManage,
        meta: { title: '公告管理', requiresAuth: true }
      },
      {
        path: 'carouseManage',
        name: 'AdminCarouseManage',
        component: CarouseManage,
        meta: { title: '轮播图', requiresAuth: true }
      },
      {
        path: 'navbar',
        name: 'AdminNavbar',
        component: NavbarManage,
        meta: { title: '导航栏管理', requiresAuth: true }
      },
      {
        path: 'home-settings',
        name: 'AdminHomeSettings',
        component: HomeSettingsManage,
        meta: { title: '首页设置', requiresAuth: true }
      },
      {
        path: 'love-room',
        name: 'AdminLoveRoom',
        component: LoveRoomManage,
        meta: { title: '恋爱小屋', requiresAuth: true }
      },
      {
        path: 'love-time-manage',
        name: 'AdminLoveTimeManage',
        component: LoveTimeManage,
        meta: { title: '恋爱时光', requiresAuth: true }
      },
      {
        path: 'life-time-manage',
        name: 'AdminLifeTimeManage',
        component: LifeTimeManage,
        meta: { title: '生活时光', requiresAuth: true }
      }
    ]
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 鉴权检查
router.beforeEach(async (to, from, next) => {
  // 如果已登录用户访问登录页面，自动跳转到后台
  if (to.path === '/admin/login') {
    // 先异步验证token是否真的有效
    const isValid = await isLoggedInAsync()
    if (isValid) {
      next('/admin')
      return
    }
  }
  
  // 检查路由是否需要鉴权
  if (to.meta.requiresAuth) {
    // 异步验证token是否真正有效
    const isValid = await isLoggedInAsync()
    
    if (!isValid) {
      // 没有登录或token已过期，跳转到登录页面并显示提示
      next({
        path: '/admin/login',
        query: { 
          redirect: to.fullPath,
          message: '登录已过期，请重新登录'
        }
      })
      return
    }
    
    // 已登录且token有效，继续访问
    next()
  } else {
    // 不需要鉴权的路由，直接访问
    next()
  }
})

export default router 