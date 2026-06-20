<template>
  <div class="header-container">
    <div class="title">
      <el-icon class="logo-icon">
        <Monitor/>
      </el-icon>
      <span>后台管理系统</span>
    </div>

    <!-- 右侧头像区域 -->
    <div class="user-section">
      <!-- 
        popper-class: 用于自定义下拉菜单的样式类名 
      -->
      <el-dropdown trigger="hover" popper-class="user-dropdown-popper">
        <div class="user-info-card">
          <img
            :src="userInfo.avatar || defaultAvatar"
            alt="avatar"
            class="avatar"
          />
          <div class="user-text">
            <span class="username">{{ userInfo.username || '管理员' }}</span>
            <el-icon class="arrow-icon">
              <CaretBottom/>
            </el-icon>
          </div>
        </div>

        <!-- 下拉菜单内容 -->
        <template #dropdown>
          <el-dropdown-menu>
            <!-- 头部装饰（可选） -->
            <div class="dropdown-header">
              <span class="header-name">{{ userInfo.username || '用户' }}</span>
              <span class="header-email">{{ userInfo.email || '未绑定邮箱' }}</span>
            </div>

            <el-dropdown-item @click="goprofile" class="menu-item">
              <el-icon>
                <User/>
              </el-icon>
              <span>个人中心</span>
            </el-dropdown-item>

            <el-dropdown-item divided @click="logout" class="menu-item logout-item">
              <el-icon>
                <SwitchButton/>
              </el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from "vue-router";
import { ref, onMounted } from "vue";
import request from "../utills/request.js";
import { logout as authLogout } from "../utills/auth.js";
import { useAuthStore } from "../stores/auth.js";
// 引入图标
import {Monitor, CaretBottom, User, SwitchButton} from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const defaultAvatar = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";

const userInfo = ref({
  username: '',
  avatar: '',
  email: '',
  phone: '',
  description: ''
});

// 获取用户信息
const getUserInfo = async () => {
  try {
    const response = await request.post('/user/getPersonalInfo');
    if (response.code === 0 && response.data) {
      userInfo.value = response.data;
    }
  } catch (error: any) {
    console.error('获取用户信息失败:', error.message);
  }
};

const logout = () => {
  authLogout();
  authStore.logout();
  router.push("/admin/login");
};

const goprofile = () => {
  router.push("/admin/profile");
};

onMounted(() => {
  getUserInfo();
});
</script>

<style scoped>
/* 顶栏容器 */
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 64px;
  position: relative;
  z-index: 100;
}

/* 左侧标题 */
.title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.logo-icon {
  font-size: 24px;
  color: #409eff;
}

/* 右侧用户区域 */
.user-section {
  display: flex;
  align-items: center;
}

/* 用户信息卡片 (触发器) */
.user-info-card {
  display: flex;
  align-items: center;
  padding: 4px 8px 4px 4px;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-info-card:hover {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.4s ease;
}

.user-info-card:hover .avatar {
  transform: scale(1.05);
}

.user-text {
  display: flex;
  align-items: center;
  margin-left: 10px;
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  margin-left: 6px;
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s;
}

/* 悬停时箭头旋转 */
.user-info-card:hover .arrow-icon {
  transform: rotate(180deg);
}
</style>

<!-- 全局样式，用于覆盖 Element Plus 下拉菜单的默认样式 -->
<style>
.user-dropdown-popper {
  /* 移除默认的边框和增加圆角 */
  border-radius: 12px !important;
  border: none !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1) !important;
  padding: 8px 0 !important;
  overflow: hidden;
}

/* 下拉菜单头部信息 */
.dropdown-header {
  padding: 12px 20px;
  border-bottom: 1px solid #f0f2f5;
  margin-bottom: 4px;
  display: flex;
  flex-direction: column;
}

.header-name {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.header-email {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 菜单项样式 */
.user-dropdown-popper .el-dropdown-menu__item {
  padding: 10px 20px !important;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 菜单项悬停 */
.user-dropdown-popper .el-dropdown-menu__item:hover,
.user-dropdown-popper .el-dropdown-menu__item:focus {
  background-color: #ecf5ff !important;
  color: #409eff !important;
}

/* 退出登录项特殊样式 */
.user-dropdown-popper .logout-item {
  color: #f56c6c;
}

.user-dropdown-popper .logout-item:hover {
  background-color: #fef0f0 !important;
  color: #f56c6c !important;
}

/* 调整小三角位置或隐藏 */
.user-dropdown-popper .el-popper__arrow {
  display: none; /* 现代风格通常隐藏这个小三角，看起来更简洁 */
}
</style>