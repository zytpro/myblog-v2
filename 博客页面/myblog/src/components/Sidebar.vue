<template>
  <div class="sidebar">
    <el-aside width="240px" class="admin-aside">

      <el-menu
        :default-active="currentPath"
        router
        active-text-color="#409eff"
        background-color="#304156"
        text-color="#fff"
      >
        <el-menu-item index="/admin" class="menu-item">
          <img src="/icon/home.svg" class="icon" alt="home" />
          <span class="menu-text">首页</span>
        </el-menu-item>

        <el-sub-menu index="3" class="menu-item">
          <template #title>
            <img src="/icon/personpage.svg" class="icon" alt="web" />
            <span class="menu-text">个人中心</span>
          </template>
          <el-menu-item index="/admin/profile">
            <img src="/icon/personal.svg" class="icon" alt="settings" />
            <span class="menu-text">个人中心</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="4" class="menu-item">
          <template #title>
            <img src="/icon/fileManage.svg" class="icon" alt="web" />
            <span class="menu-text">文件管理</span>
          </template>
          <el-menu-item index="/admin/picture">
            <img src="/icon/picture.svg" class="icon" alt="settings" />
            <span class="menu-text">图片</span>
          </el-menu-item>
          <el-menu-item index="/admin/video">
            <img src="/icon/video.svg" class="icon" alt="settings" />
            <span class="menu-text">视频</span>
          </el-menu-item>
          <el-menu-item index="/admin/music">
            <img src="/icon/mucis.svg" class="icon" alt="settings" />
            <span class="menu-text">音乐</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="2" class="menu-item">
          <template #title>
            <img src="/icon/article.png" class="icon" alt="document" />
            <span class="menu-text">文章管理</span>
          </template>
          <el-menu-item index="/admin/article">
            <img src="/icon/article.svg" class="icon" alt="settings" />
            <span class="menu-text">文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/category">
            <img src="/icon/category.svg" class="icon" alt="settings" />
            <span class="menu-text">分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/tags">
            <img src="/icon/tags.svg" class="icon" alt="settings" />
            <span class="menu-text">标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/commentManage">
            <img src="/icon/talk.png" class="icon" alt="settings" />
            <span class="menu-text">评论管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="5" class="menu-item">
          <template #title>
            <img src="/icon/hotsearch.svg" class="icon" alt="web" />
            <span class="menu-text">热搜管理</span>
          </template>
          <el-menu-item index="/admin/hotSearch">
            <img src="/icon/hotsearch.svg" class="icon" alt="settings" />
            <span class="menu-text">热搜api</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="6" class="menu-item">
          <template #title>
            <img src="/icon/announce.svg" class="icon" alt="web" />
            <span class="menu-text">公告管理</span>
          </template>
          <el-menu-item index="/admin/announcement">
            <img src="/icon/announce.svg" class="icon" alt="settings" />
            <span class="menu-text">公告管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="7" class="menu-item">
          <template #title>
            <img src="/icon/lunbotu.svg" class="icon" alt="web" />
            <span class="menu-text">轮播图管理</span>
          </template>
          <el-menu-item index="/admin/carouseManage">
            <img src="/icon/lunbotu.svg" class="icon" alt="settings" />
            <span class="menu-text">轮播图</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="8" class="menu-item">
          <template #title>
            <img src="/icon/web.png" class="icon" alt="web" />
            <span class="menu-text">网站管理</span>
          </template>
          <el-menu-item index="/admin/navbar">
            <img src="/icon/navbar.svg" class="icon" alt="settings" />
            <span class="menu-text">导航栏管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/home-settings">
            <img src="/icon/home.svg" class="icon" alt="home" />
            <span class="menu-text">首页设置</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="9" class="menu-item">
          <template #title>
            <img src="/icon/loveroom.svg" class="icon" alt="love" />
            <span class="menu-text">恋爱空间</span>
          </template>
          <el-menu-item index="/admin/love-room">
            <img src="/icon/loveroom.svg" class="icon" alt="love" />
            <span class="menu-text">恋爱小屋</span>
          </el-menu-item>
          <el-menu-item index="/admin/love-time-manage">
            <img src="/icon/time.svg" class="icon" alt="time" />
            <span class="menu-text">恋爱时光</span>
          </el-menu-item>
          <el-menu-item index="/admin/life-time-manage">
            <img src="/icon/life.svg" class="icon" alt="time" />
            <span class="menu-text">生活时光</span>
          </el-menu-item>
        </el-sub-menu>

      </el-menu>
    </el-aside>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from "vue-router";
import { computed, ref, onMounted } from "vue";
import request from "../utills/request.js";

const route = useRoute();
const currentPath = computed(() => route.path);

const navbarList = ref<any[]>([]);
const fetchNavbar = async () => {
  try {
    const res: any = await request.get('/navbar/list');
    let list: any[] = [];
    if (Array.isArray(res)) list = res;
    else if (Array.isArray(res?.data)) list = res.data;
    navbarList.value = list.filter(i => i.state === '1' || i.state === 1);
  } catch (e) {
    navbarList.value = [];
  }
};

onMounted(fetchNavbar);
</script>

<style scoped>
.sidebar {
  user-select: none; 
  cursor: pointer; 
}

.icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
}

.admin-aside {
  background-color: #304156;
  height: 100vh;
  overflow-y: auto;
}

.menu-item .menu-text {
  user-select: none; 
  cursor: pointer;
}

.el-menu {
  border-right: none;
}

.el-menu-item {
  color: #fff !important;
}

.el-menu-item:hover {
  background-color: #263445 !important;
}

.el-sub-menu__title {
  color: #fff !important;
}

.el-sub-menu__title:hover {
  background-color: #263445 !important;
}

.el-menu-item.is-active {
  background-color: #409eff !important;
  color: #fff !important;
}

.nav-tags { padding: 10px; display: flex; flex-wrap: wrap; gap: 6px; }
.nav-tag { cursor: default; }
</style>
