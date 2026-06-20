<template>
  <div class="login-wrapper">
    <TechBackground />

    <div class="login-card">
      <div class="login-header">
        <div class="login-icon-wrap">
          <svg class="login-shield" viewBox="0 0 64 64" width="48" height="48" fill="none">
            <path d="M32 4L8 16v20c0 19.2 10.67 27.73 24 32 13.33-4.27 24-12.8 24-32V16L32 4z"
                  fill="none" stroke="rgba(100,200,255,0.9)" stroke-width="2.5" stroke-linejoin="round"/>
            <path d="M26 34l4 4 8-8" stroke="rgba(100,200,255,0.9)" stroke-width="2.5"
                  stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="login-title">后台管理</div>
        <div class="login-sub">安全认证 · 管理员登录</div>
      </div>

      <form class="form-inner" @submit.prevent="onSubmit">
        <div class="form-group">
          <label class="label">
            <svg class="label-ico" viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
            用户名
          </label>
          <v-text-field
            v-model="form.username"
            variant="outlined"
            density="comfortable"
            hide-details
            placeholder="请输入用户名"
            class="vuetify-input"
            required
          />
        </div>
        <div class="form-group">
          <label class="label">
            <svg class="label-ico" viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM12 17c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1s3.1 1.39 3.1 3.1v2z"/>
            </svg>
            密码
          </label>
          <v-text-field
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            variant="outlined"
            density="comfortable"
            hide-details
            placeholder="请输入密码"
            class="vuetify-input"
            :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword"
            required
          />
        </div>
        <v-btn
          color="primary"
          block
          class="login-btn"
          type="submit"
          :loading="loading"
          :disabled="loading"
        >
          {{ loading ? '验证中...' : '登 录' }}
        </v-btn>
      </form>

      <div class="login-footer">
        <div class="scan-line"></div>
      </div>
    </div>

    <!-- 消息提示 -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="3000"
      location="top"
    >
      <div class="d-flex align-center">
        <v-icon :icon="snackbar.icon" class="mr-2"></v-icon>
        {{ snackbar.message }}
      </div>
      <template v-slot:actions>
        <v-btn color="white" variant="text" @click="snackbar.show = false">
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utills/request.js'
import { setToken } from '../../utills/auth.js'
import { useAuthStore } from '../../stores/auth.js'
import TechBackground from '../../components/TechBackground.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showPassword = ref(false)
const loading = ref(false)
const form = ref({
  username: '',
  password: ''
})

const snackbar = ref({
  show: false,
  message: '',
  color: 'info',
  icon: 'mdi-information'
})

async function onSubmit() {
  if (!form.value.username || !form.value.password) {
    showMessage('用户名或密码不能为空', 'error')
    return
  }

  try {
    loading.value = true
    const response = await request.post('/user/login', {
      username: form.value.username,
      password: form.value.password
    })

    if (response.code === 200) {
      const token = response.data.tokenValue
      setToken(token)
      authStore.setLoggedIn(true)
      showMessage('登录成功', 'success')
      const redirectPath = route.query.redirect || '/admin'
      router.push(redirectPath)
    } else {
      showMessage(response.msg || '登录失败', 'error')
    }
  } catch (error) {
    console.error('登录失败:', error)
    showMessage('服务器错误，请稍后再试', 'error')
  } finally {
    loading.value = false
  }
}

function showMessage(message, type = 'info') {
  snackbar.value = {
    show: true,
    message: message,
    color: type === 'error' ? 'error' : type === 'success' ? 'success' : 'info',
    icon: type === 'error' ? 'mdi-alert-circle' : type === 'success' ? 'mdi-check-circle' : 'mdi-information'
  }
}
</script>

<style scoped>
.login-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 0;
}

/* ---- 卡片主体 ---- */
.login-card {
  position: relative;
  z-index: 2;
  width: 420px;
  backdrop-filter: blur(20px) saturate(180%);
  background: rgba(12, 17, 25, 0.7);
  border: 1px solid rgba(100, 200, 255, 0.15);
  border-radius: 16px;
  padding: 32px 28px;
  box-shadow:
    0 0 60px rgba(100, 200, 255, 0.08),
    0 16px 40px rgba(0, 0, 0, 0.5);
  color: #fff;
}

/* ---- 头部 ---- */
.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-icon-wrap {
  margin-bottom: 14px;
}

.login-shield {
  filter: drop-shadow(0 0 12px rgba(100, 200, 255, 0.4));
  animation: shieldPulse 3s ease-in-out infinite;
}

@keyframes shieldPulse {
  0%, 100% { filter: drop-shadow(0 0 12px rgba(100, 200, 255, 0.4)); }
  50% { filter: drop-shadow(0 0 22px rgba(140, 100, 255, 0.6)); }
}

.login-title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #64c8ff 0%, #8c64ff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 6px;
}

.login-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 2px;
}

/* ---- 表单 ---- */
.form-inner {
  max-width: 320px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 18px;
}

.label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
  margin: 0 0 8px 2px;
  letter-spacing: 1px;
}

.label-ico {
  opacity: 0.6;
}

.vuetify-input :deep(.v-field) {
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #fff;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.vuetify-input :deep(.v-field__input) {
  color: #fff;
}

.vuetify-input :deep(.v-field--focused) {
  border-color: rgba(100, 200, 255, 0.4);
  box-shadow: 0 0 16px rgba(100, 200, 255, 0.10);
}

.vuetify-input :deep(.v-field__append-inner) {
  color: rgba(255, 255, 255, 0.5);
}

.login-btn {
  margin-top: 14px;
  height: 44px !important;
  font-size: 15px !important;
  letter-spacing: 4px !important;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #3a7bd5 0%, #6c5ce7 100%) !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(108, 92, 231, 0.3) !important;
  transition: all 0.3s ease !important;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(108, 92, 231, 0.45) !important;
}

/* ---- 底部 ---- */
.login-footer {
  margin-top: 22px;
  text-align: center;
}

.scan-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(100, 200, 255, 0.3), transparent);
}

/* ---- 响应式 ---- */
@media (max-width: 480px) {
  .login-card {
    width: 92vw;
    padding: 24px 18px;
  }
  .login-title {
    font-size: 20px;
  }
}
</style>
