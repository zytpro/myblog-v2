<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-title">后台管理登录</div>
      <div class="login-sub">使用管理员账号登录系统</div>
      <form class="form-inner" @submit.prevent="onSubmit">
        <div class="form-group">
          <label for="username" class="label">用户名</label>
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
          <label for="password" class="label">密码</label>
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
          {{ loading ? '登录中...' : '登录' }}
        </v-btn>
      </form>
    </div>

    <!-- 背景层 -->
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>

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
        <v-btn
          color="white"
          variant="text"
          @click="snackbar.show = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utills/request.js'
import { setToken } from '../../utills/auth.js'
import { useAuthStore } from '../../stores/auth.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showPassword = ref(false)
const loading = ref(false)
const form = ref({
  username: '',
  password: ''
})

// Snackbar状态
const snackbar = ref({
  show: false,
  message: '',
  color: 'info',
  icon: 'mdi-information'
})

let addedLinks = []
let addedScripts = []

onMounted(() => {
  // 检查是否有登录过期提示
  if (route.query.message) {
    showMessage(route.query.message, 'error')
  }
  
  const cssHrefs = ['/html/9/normalize.min.css', '/html/9/style.css']
  cssHrefs.forEach(href => {
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = href
    document.head.appendChild(link)
    addedLinks.push(link)
  })
  const jsSrcs = ['/html/9/modernizr.min.js', '/html/9/prefixfree.min.js', '/html/9/jquery.min.js', '/html/9/script.js']
  jsSrcs.forEach(src => {
    const s = document.createElement('script')
    s.src = src
    s.defer = true
    document.body.appendChild(s)
    addedScripts.push(s)
  })
})

onBeforeUnmount(() => {
  addedLinks.forEach(l => l.parentNode && l.parentNode.removeChild(l))
  addedScripts.forEach(s => s.parentNode && s.parentNode.removeChild(s))
})

// 处理表单提交
async function onSubmit() {
  // 验证表单
  if (!form.value.username || !form.value.password) {
    showMessage('用户名或密码不能为空', 'error')
    return
  }

  try {
    loading.value = true
    
    // 调用登录API
    const response = await request.post('/user/login', {
      username: form.value.username,
      password: form.value.password
    })

    if (response.code === 200) {
      // 登录成功
      const token = response.data.tokenValue
      setToken(token)
      authStore.setLoggedIn(true) // 更新登录状态
      showMessage('登录成功', 'success')
      
      // 跳转到后台管理页面，如果有重定向地址则跳转到重定向地址
      const redirectPath = route.query.redirect || '/admin'
      router.push(redirectPath)
    } else {
      // 登录失败
      showMessage(response.msg || '登录失败', 'error')
    }
  } catch (error) {
    console.error('登录失败:', error)
    showMessage('服务器错误，请稍后再试', 'error')
  } finally {
    loading.value = false
  }
}

// 显示消息提示
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
  pointer-events: none;
  padding: 16px;
}
.login-card {
  width: 420px;
  backdrop-filter: blur(14px);
  background: rgba(20, 24, 31, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 16px;
  padding: 28px 24px;
  box-shadow: 0 16px 40px rgba(0,0,0,0.45);
  color: #fff;
  pointer-events: all;
}
.form-inner { max-width: 320px; margin: 0 auto; }
.login-title, .login-sub { text-align: center; }
.login-title { font-size: 22px; font-weight: 800; margin: 0 0 8px; letter-spacing: .5px; }
.login-sub { font-size: 13px; opacity: 0.85; margin-bottom: 18px; }
.form-group { margin-bottom: 16px; }
.label { display: block; font-size: 12px; color: rgba(255,255,255,.8); margin: 0 0 6px; }
/* 统一 Vuetify 输入外观为深色玻璃风 */
.vuetify-input :deep(.v-field) {
  border-radius: 10px;
  background: rgba(255,255,255,0.06);
  color: #fff;
}
.vuetify-input :deep(.v-field__input) {
  color: #fff;
}
.vuetify-input :deep(.v-field--focused) {
  box-shadow: 0 0 0 3px rgba(66,184,131,0.25);
}
.login-btn { margin-top: 10px; }

/* Snackbar样式 */
:deep(.v-snackbar) {
  z-index: 9999;
}

:deep(.v-snackbar .v-snackbar__content) {
  font-weight: 500;
  letter-spacing: 0.5px;
}

:deep(.v-snackbar .v-btn) {
  color: inherit;
  font-weight: 500;
}
</style> 