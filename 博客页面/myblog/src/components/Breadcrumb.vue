<template>
  <div class="breadcrumb">
    <el-breadcrumb>
    <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index" :to="item.path">
      {{ item.meta?.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>

    <div class="breadcrumb-tags" v-if="history.length">
      <el-tag
        v-for="tag in history"
        :key="tag.path"
        type="info"
        class="crumb-tag"
        closable
        @close="removeTag(tag.path)"
        @click="go(tag.path)"
      >
        {{ tag.title }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBreadcrumbStore } from '../stores/breadcrumb'

const route = useRoute()
const router = useRouter()
const breadcrumbList = ref<any[]>([])
const store = useBreadcrumbStore()
const history = computed(() => store.history)

const addCurrentToHistory = () => {
  const title = route.meta?.title as string | undefined
  const path = route.path
  if (title && path && path !== '/admin') {
    store.add({ path, title })
  }
}

// 生成面包屑数据
const getBreadcrumb = () => {
  const matched = route.matched
  const breadcrumbItems = matched.filter(item => item.meta?.title)
  breadcrumbList.value = breadcrumbItems
  addCurrentToHistory()
}

const go = (path: string) => router.push(path)
const removeTag = (path: string) => {
  store.remove(path)
  // 如果移除的是当前页，跳转到最后一个历史或首页
  if (route.path === path) {
    const last = store.history[store.history.length - 1]
    router.push(last ? last.path : '/admin')
  }
}

watch(() => route.path, () => { getBreadcrumb() }, { immediate: true })
</script>

<style scoped>
.breadcrumb { padding: 15px 0; margin-bottom: 8px; }
:deep(.el-breadcrumb__item .el-breadcrumb__inner) { color: #666; }
:deep(.el-breadcrumb__item .el-breadcrumb__inner.is-link) { color: #409EFF; cursor: pointer; }
:deep(.el-breadcrumb__item .el-breadcrumb__inner.is-link:hover) { color: #66b1ff; }
:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) { color: #999; }

.breadcrumb-tags { margin-top: 8px; display: flex; gap: 8px; flex-wrap: wrap; }
.crumb-tag { cursor: pointer; user-select: none; }
</style> 