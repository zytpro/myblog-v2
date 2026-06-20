
import axios from "axios";
import { ElMessage } from "element-plus";
import router from "../router";
import { getToken, removeToken } from "./auth.js";
import { useAuthStore } from "../stores/auth.js";
import { API_BASE_URL } from "../config/env.js";

// 创建 axios 实例
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

// 请求拦截器
axiosInstance.interceptors.request.use(
  (config) => {
    // 获取 Token
    const token = getToken();
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    
    // 只有在非 FormData 请求时才设置 Content-Type
    if (!(config.data instanceof FormData)) {
      config.headers["Content-Type"] = "application/json";
    }
    
    return config;
  },
  (error) => {
    console.error("[Request Error]:", error);
    return Promise.reject(error);
  },
);

// 响应拦截器
axiosInstance.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        // 清除过期的token
        removeToken();
        
        // 更新登录状态
        const authStore = useAuthStore();
        authStore.logout();
        
        // 如果当前不在登录页面，则跳转到登录页面
        if (router.currentRoute.value.path !== '/admin/login') {
          ElMessage.error("登录已过期，请重新登录");
          router.push({
            path: "/admin/login",
            query: { 
              redirect: router.currentRoute.value.fullPath,
              message: '登录已过期，请重新登录'
            }
          });
        }
      } else {
        ElMessage.error(data.message || "请求失败");
      }
    } else {
      ElMessage.error("网络错误，请稍后重试");
    }
    return Promise.reject(error);
  },
);

export default axiosInstance;
